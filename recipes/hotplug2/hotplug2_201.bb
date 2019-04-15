DESCRIPTION = "openwrt mtd"
PR = "r0"
SRC_URI = "file://${PN}-${PV}.tar.gz \
           file://100-env_memleak.patch \
           file://110-static_worker.patch \
           file://120-sysfs_path_fix.patch \
           file://130-cancel_download_fix.patch \
           file://140-worker_fork_fix.patch \
           file://150-force_fork_slow.patch \
	   file://160-event_block_fix.patch \
	   file://05-wifi \
           file://hotplug2.rules"

S = "${WORKDIR}/${PN}-${PV}"

EXTRA_OEMAKE = "'STATIC_WORKER=fork' 'LDFLAGS=${LDFLAGS}'"

do_install() {
	install -d -m0755 ${D}/etc
	install -m0644 ${WORKDIR}/hotplug2.rules ${D}/etc

	install -d -m0755 ${D}/sbin
	install -m0755 hotplug2  ${D}/sbin

	install -d -m0755 ${D}/etc/hotplug.d/iface
	install -m0755 ${WORKDIR}/05-wifi ${D}/etc/hotplug.d/iface
}
