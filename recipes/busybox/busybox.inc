DESCRIPTION = "BusyBox combines tiny versions of many common UNIX utilities into a single \
small executable. It provides minimalist replacements for most of the \
utilities you usually find in GNU fileutils, shellutils, etc. The utilities \
in BusyBox generally have fewer options than their full-featured GNU \
cousins; however, the options that are included provide the expected \
functionality and behave very much like their GNU counterparts. BusyBox \
provides a fairly complete POSIX environment for any small or embedded \
system."
HOMEPAGE = "http://www.busybox.net"
LICENSE = "GPLv2"
SECTION = "base"
PRIORITY = "required"

INC_PR = "r35"

SRC_URI = "\
  file://busybox-cron \
  file://busybox-httpd \
  file://busybox-udhcpd \
  file://default.script file://simple.script \
  file://dhcp-hostname.patch \
  file://hwclock.sh \
  file://hwclock-default \
  file://ifupdown-spurious-environ.patch \
  file://mount.busybox \
  file://syslog \
  file://syslog.conf \
  file://udhcpscript.patch \
  file://umount.busybox \
"

SRC_URI_append_nylon = " file://xargs-double-size.patch"

export EXTRA_CFLAGS = "${CFLAGS}"
EXTRA_OEMAKE_append = " CROSS=${HOST_PREFIX}"

# We need this RRECOMMENDS_${PN} because libc dlopens libgcc
# and shlib mechanism can not detect it because its not
# listed in the NEEDED field.
RRECOMMENDS_${PN} += "libgcc"


# Make busybox recommend busybox-syslog for those images that expect it

FILES_${PN} += "${datadir}/udhcpc"

# syslog initscript is handled explicitly because order of
# update-rc.d and update-alternatives is important (see below)

# This disables the syslog startup links in slugos (see slugos-init)
# Use gcc for linking so LDFLAGS actually makes sense
LD = "${CC} -nostdlib"

inherit cml1
require busybox-config.inc

configmangle = '/CROSS_COMPILER_PREFIX/d; \
		/CONFIG_EXTRA_CFLAGS/d; \
		'
OE_FEATURES := "${@features_to_busybox_conf(d)}"
OE_DEL      := "${@features_to_busybox_del(d)}"
DO_IPv4 := ${@base_contains('DISTRO_FEATURES', 'ipv4', 1, 0, d)}
DO_IPv6 := ${@base_contains('DISTRO_FEATURES', 'ipv6', 1, 0, d)}
python () {
  if "${OE_DEL}":
    bb.data.setVar('configmangle_append', "${OE_DEL}" + "\n", d)
  if "${OE_FEATURES}":
    bb.data.setVar('configmangle_append',
                   "/^### DISTRO FEATURES$/a\\\n%s\n\n" %
                   ("\\n".join((bb.data.expand("${OE_FEATURES}", d).split("\n")))),
                   d)
  bb.data.setVar('configmangle_append',
                 "/^### CROSS$/a\\\n%s\n" %
                  ("\\n".join(["CONFIG_CROSS_COMPILER_PREFIX=\"${TARGET_PREFIX}\"",
			       "CONFIG_EXTRA_CFLAGS=\"${CFLAGS}\""
                        ])
                  ),
                 d)
}

do_configure () {
	sed -e 's#@DATADIR@#${datadir}#g' \
		< ${WORKDIR}/defconfig > ${S}/.config
	for i in 'CROSS' 'DISTRO FEATURES'; do echo "### $i"; done >> \
		${S}/.config
	sed -i -e '${configmangle}' ${S}/.config
	if test ${DO_IPv4} -eq 0 && test ${DO_IPv6} -eq 0; then
	  # disable networking applets
	  mv ${S}/.config ${S}/.config.oe-tmp
	  awk 'BEGIN{net=0}
	  /^# Networking Utilities/{net=1}
	  /^#$/{if(net){net=net+1}}
	  {if(net==2&&$0 !~ /^#/&&$1){print("# "$1" is not set")}else{print}}' \
		  ${S}/.config.oe-tmp > ${S}/.config
	fi
	cml1_do_configure
}

do_compile() {
	unset CFLAGS CPPFLAGS CXXFLAGS
	base_do_compile
	# Busybox will force a strip, we do not want that.  Future-proof
	# in case later versions do this better.
	cp busybox_unstripped busybox || true
}

do_install () {
	oe_runmake busybox.links
	if [ "${prefix}" != "/usr" ]; then
		sed "s:^/usr/:${prefix}/:" busybox.links > busybox.links.new
		mv busybox.links.new busybox.links
	fi
	if [ "${base_sbindir}" != "/sbin" ]; then
		sed "s:^/sbin/:${base_sbindir}/:" busybox.links > busybox.links.new
		mv busybox.links.new busybox.links
	fi

	install -d ${D}${sysconfdir}/init.d

	if ! grep -q "CONFIG_FEATURE_INDIVIDUAL=y" ${WORKDIR}/defconfig; then
		# Install /bin/busybox, and the /bin/sh link so the postinst script
		# can run. Let update-alternatives handle the rest.
		install -d ${D}${base_bindir}
		if grep -q "CONFIG_FEATURE_SUID=y" ${WORKDIR}/defconfig; then
			install -m 4755 ${S}/busybox ${D}${base_bindir}
		else
			install -m 0755 ${S}/busybox ${D}${base_bindir}
		fi
		ln -sf busybox ${D}${base_bindir}/sh
	else
		install -d ${D}${base_bindir} ${D}${base_sbindir}
		install -d ${D}${libdir} ${D}${bindir} ${D}${sbindir}
		cat busybox.links | while read FILE; do
			NAME=`basename "$FILE"`
			install -m 0755 "0_lib/$NAME" "${D}$FILE.${PN}"
		done
		# add suid bit where needed
		for i in `grep -E "APPLET.*_BB_SUID_((MAYBE|REQUIRE))" include/applets.h | grep -v _BB_SUID_DROP | cut -f 3 -d '(' | cut -f 1 -d ','`; do
			find ${D} -name $i.${PN} -exec chmod a+s {} \;
		done
		install -m 0755 0_lib/libbusybox.so.${PV} ${D}${libdir}/libbusybox.so.${PV}
		ln -sf sh.${PN} ${D}${base_bindir}/sh
		ln -sf ln.${PN} ${D}${base_bindir}/ln
		ln -sf test.${PN} ${D}${bindir}/test
		if [ -f ${D}/linuxrc.${PN} ]; then
			mv ${D}/linuxrc.${PN} ${D}/linuxrc
		fi
	fi

	install -m 0644 ${S}/busybox.links ${D}${sysconfdir}
}

pkg_postinst_${PN} () {
	# If we are not making an image we create links for the utilities that doesn't exist
	# so the update-alternatives script will get the utilities it needs
	# (update-alternatives have no problem replacing links later anyway)
	test -n 2> /dev/null || alias test='busybox test'
	ln --help >/dev/null 2>&1 || alias ln='busybox ln'
	if test "x$D" = "x"; then
		while read link; do
			if test ! -h "$link"; then
				if test -f "$link.busybox"; then
					to="$link.busybox"
				else
					case "$link" in
						/*/*/*) to="../../bin/busybox";;
						/bin/*) to="busybox";;
						/*/*) to="../bin/busybox";;
						/*) to="/bin/busybox";;
					esac
				fi
				ln -s $to $link
			fi
		done </etc/busybox.links
	fi

	# This adds the links, remember that this has to work when building an image too, hence the $D
	set +e
	while read link; do
		if test -f "$D$link.${PN}"; then
			to="$link.${PN}"
		else
			case "$link" in
				/*/*/*) to="../../bin/busybox";;
				/bin/*) to="busybox";;
				/*/*) to="../bin/busybox";;
				/*) to="/bin/busybox";;
			esac
		fi
		bn=`basename $link`
		update-alternatives --install $link $bn $to 50
	done <$D/etc/busybox.links
	set -e
}

