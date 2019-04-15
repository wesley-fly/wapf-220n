DESCRIPTION = "wled"
SECTION = "base"
PRIORITY = "optional"
LICENSE = "GPL"


INITSCRIPT_NAME = "wled"

SRC_URI = "\
	file://wled.c \
	file://Makefile \
"

S = "${WORKDIR}"

inherit module update-rc.d

do_install() {
        install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/net/
        install -m 0644 ${WORKDIR}/wled.ko ${D}${base_libdir}/modules/${KERNEL_VERSION}/net/
}

