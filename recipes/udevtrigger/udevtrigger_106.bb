DESCRIPTION = "udevtrigger"
PR = "r0"
SRC_URI = "file://udev-${PV}.tar.bz2 \
           file://001-no_debug.patch \
           file://002-udevtrigger_no_config.patch"

S = "${WORKDIR}/udev-${PV}"

inherit autotools

#EXTRA_OEMAKE="'CROSS_COMPILE=powerpc-linux-gnu-'"
EXTRA_OEMAKE="'CC=${CC}' 'LD=${CC}' 'AR=${AR}' 'RANLIB=${RANLIB}' 'LDFLAGS=${LDFLAGS}' 'CFLAGS=${CFLAGS}'"

do_compile() {
	oe_runmake  udevtrigger
}

do_install() {
	install -d -m0755 ${D}${base_sbindir}
	install -m0755 udevtrigger ${D}${base_sbindir}
}

