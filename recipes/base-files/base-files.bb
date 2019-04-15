DESCRIPTION = "base files"
PR = "r0"
SRC_URI = "file://${PN}.tar.gz \
	   file://wifi \
	   file://active_wifi \
	   file://network \
	   file://fixrate \
	   file://shadow \
	   file://profile \
	   file://rcS \
	   file://backup \
	   file://sysupgrade.conf"

S = "${WORKDIR}/${PN}"

do_compile() {
}

do_install() {

	cp -fpR * ${D}

	install -d -m0755 ${D}/sbin
	install -m0755 ${WORKDIR}/active_wifi ${D}/sbin
	install -m0755 ${WORKDIR}/wifi ${D}/sbin/wifi
	install -m0755 ${WORKDIR}/fixrate ${D}/sbin


	install -d -m0755 ${D}${sysconfdir}
	install -d -m0755 ${D}${sysconfdir}/config
	install -m0644 ${WORKDIR}/network ${D}${sysconfdir}/config
	install -m0644 ${WORKDIR}/sysupgrade.conf ${D}${sysconfdir}

	install -d -m0755 ${D}${sysconfdir}/rc.d
	install -m0755 ${WORKDIR}/rcS ${D}/etc/init.d
	install -m0755 ${WORKDIR}/profile ${D}/etc/
	install -m0755 ${WORKDIR}/backup ${D}/etc/backup.tar.gz

	ln -s /tmp/fstab ${D}${sysconfdir}/fstab
	ln -s /proc/mounts ${D}${sysconfdir}/mtab
	ln -s /tmp/resolv.conf ${D}${sysconfdir}/resolv.conf
	ln -s /tmp/TZ ${D}${sysconfdir}/TZ
	ln -s /tmp ${D}/var

	ln -s ../init.d/dropbear ${D}${sysconfdir}/rc.d/K50dropbear
	ln -s ../init.d/network  ${D}${sysconfdir}/rc.d/K90network
	ln -s ../init.d/luci_fixtime    ${D}${sysconfdir}/rc.d/K95luci_fixtime
	ln -s ../init.d/boot     ${D}${sysconfdir}/rc.d/K98boot
	ln -s ../init.d/umount   ${D}${sysconfdir}/rc.d/K99umount
	ln -s ../init.d/luci_fixtime ${D}${sysconfdir}/rc.d/S05luci_fixtime

	ln -s ../init.d/boot     ${D}${sysconfdir}/rc.d/S10boot
	ln -s ../init.d/usb      ${D}${sysconfdir}/rc.d/S39usb
	ln -s ../init.d/network  ${D}${sysconfdir}/rc.d/S40network
	ln -s /sbin/active_wifi  ${D}${sysconfdir}/rc.d/S44wifi
	ln -s ../init.d/firewall ${D}${sysconfdir}/rc.d/S45firewall
	ln -s ../init.d/cron     ${D}${sysconfdir}/rc.d/S50cron
	ln -s ../init.d/dropbear ${D}${sysconfdir}/rc.d/S42dropbear
	ln -s ../init.d/telnet   ${D}${sysconfdir}/rc.d/S50telnet
	ln -s ../init.d/uhttpd   ${D}${sysconfdir}/rc.d/S50uhttpd
	ln -s ../init.d/luci_dhcp_migrate ${D}${sysconfdir}/rc.d/S59luci_dhcp_migrate
	ln -s ../init.d/dnsmasq  ${D}${sysconfdir}/rc.d/S60dnsmasq
	ln -s ../init.d/done     ${D}${sysconfdir}/rc.d/S95done
	ln -s ../init.d/led      ${D}${sysconfdir}/rc.d/S96led
	ln -s ../init.d/watchdog ${D}${sysconfdir}/rc.d/S97watchdog
	ln -s ../init.d/sysntpd  ${D}${sysconfdir}/rc.d/S98sysntpd
	ln -s ../init.d/sysctl   ${D}${sysconfdir}/rc.d/S99sysctl
}

FILES_${PN} += "/usr/lib/common.awk"
FILES_${PN} += "/lib/functions.sh"
FILES_${PN} += "/lib/network/config.sh"
FILES_${PN} += "/lib/upgrade/*"
FILES_${PN} += "/lib/firstboot/*"
FILES_${PN} += "/lib/functions/boot.sh"
FILES_${PN} += "/lib/preinit/*"
FILES_${PN} += "/usr/share/udhcpc/default.script"
FILES_${PN} += "/rom/note"
