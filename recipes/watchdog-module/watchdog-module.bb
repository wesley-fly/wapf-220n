DESCRIPTION = "watchdog module"
SECTION = "base"
PRIORITY = "optional"
LICENSE = "GPL"


INITSCRIPT_NAME = "watchdog-module"

SRC_URI = "\
	file://watchdog.c \
	file://Makefile \
"

S = "${WORKDIR}"

inherit module update-rc.d

do_install() {
        install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/net/
        install -m 0644 ${WORKDIR}/watchdog.ko ${D}${base_libdir}/modules/${KERNEL_VERSION}/net/
}

