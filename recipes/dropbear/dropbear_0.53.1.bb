DESCRIPTION = "openwrt dropbear"
PR = "r0"
SRC_URI = "file://${PN}-${PV}.tar.gz \
           file://100-pubkey_path.patch \
           file://110-change_user.patch \
	   file://120-openwrt_options.patch \
	   file://130-ssh_ignore_o_and_x_args.patch \
           file://150-dbconvert_standalone.patch \
	   file://160-segfault_fwd_localhost.patch \
           file://200-lcrypt_bsdfix.patch \
           file://300-ipv6_addr_port_split.patch \
           file://400-CVE-2012-0920.patch \
           file://dropbear.config \
           file://dropbear.init"

S = "${WORKDIR}/${PN}-${PV}"

inherit autotools

EXTRA_OECONF = "--disable-nls --with-shared --disable-pam --enable-openpty --enable-syslog  --disable-lastlog --disable-utmp --disable-utmpx --disable-wtmp --disable-wtmpx --disable-loginfunc --disable-pututline --disable-pututxline --disable-zlib --enable-bundled-libtom"

EXTRA_OEMAKE = "'PROGRAMS=dropbear dbclient dropbearkey scp' 'MULTI=1'  'SCPPROGRESS=1'"

do_install() {
	install -d -m0755 ${D}${sbindir}
	install -m0755 dropbearmulti ${D}${sbindir}/dropbear
	
	install -d -m0755 ${D}${bindir}
	ln -s ../sbin/dropbear	${D}${bindir}/scp
	ln -s ../sbin/dropbear	${D}${bindir}/ssh
	ln -s ../sbin/dropbear	${D}${bindir}/dbclient
	ln -s ../sbin/dropbear	${D}${bindir}/dropbearkey

	install -d -m0755 ${D}${sysconfdir}/dropbear

	install -d -m0755 ${D}${sysconfdir}/config
	install -m0644 ${WORKDIR}/dropbear.config ${D}${sysconfdir}/config/dropbear

	install -d -m0755 ${D}${sysconfdir}/init.d
	install -m0744 ${WORKDIR}/dropbear.init  ${D}${sysconfdir}/init.d/dropbear
}
