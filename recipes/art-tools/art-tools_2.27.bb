DESCRIPTION = "Art tools"
PR = "r0"
SRC_URI = "file://${PN}_${PV}.tar.gz"

S = "${WORKDIR}/art-tools_${PV}"

INITSCRIPT_NAME = "art-tools"

inherit module update-rc.d

do_compile() {
	cd driver/linux && make -f makefile.artmod LDFLAGS= POWERPC=1 KERNELPATH=${STAGING_KERNEL_DIR} clean all
        cd ../../art && make -f makefile.nart POWERPC=1 clean all
}
do_install() {
	install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/net
	install -m 0644 driver/linux/modules/art.ko ${D}${base_libdir}/modules/${KERNEL_VERSION}/net
	install -m 0755 art/obj/nart.out ${D}${base_libdir}/modules/${KERNEL_VERSION}/net
}

FILES_${PN} += "/usr/bin/nart.out"

