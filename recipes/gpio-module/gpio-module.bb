DESCRIPTION = "gpio module"
SECTION = "base"
PRIORITY = "optional"
LICENSE = "GPL"


INITSCRIPT_NAME = "gpio-module"

SRC_URI = "\
	file://gpio.c \
	file://Makefile \
"

S = "${WORKDIR}"

inherit module update-rc.d

do_install() {
        install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/net/
        install -m 0644 ${WORKDIR}/gpio.ko ${D}${base_libdir}/modules/${KERNEL_VERSION}/net/
}

