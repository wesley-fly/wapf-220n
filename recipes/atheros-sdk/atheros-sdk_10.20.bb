DESCRIPTION = “atheros sdk”

SRC_URI = "\
  file://LSDK-9.2.0_U10.1020.tar.gz \
  file://LSDK-WLAN-9.2.0_U10.1020.tar.gz"

INITSCRIPT_NAME="atheros-sdk"

S = "${WORKDIR}"

inherit module

do_compile() {
	cd build
	make LDFLAGS= INSTALL_ROOT=${D} KERNELPATH=${STAGING_KERNEL_DIR} TOOLPREFIX=powerpc-linux-gnu- OS=linux KERNELARCH=powerpc TARGETARCH=powerpc-be-elf BOARD_TYPE=pb9x-2.6.31 rootfs_prep

	CFLAGS='-mcpu=8548 -mspe=yes -mabi=spe -mhard-float -mfloat-gprs=double' LDFLAGS= make INSTALL_ROOT=${D} KERNELPATH=${STAGING_KERNEL_DIR} TOOLPREFIX=powerpc-linux-gnu- OS=linux KERNELARCH=powerpc TARGETARCH=powerpc-be-elf BOARD_TYPE=pb9x-2.6.31 cgi

	CFLAGS='-mcpu=8548 -mspe=yes -mabi=spe -mhard-float -mfloat-gprs=double' LDFLAGS='${LDFLAGS}' make INSTALL_ROOT=${D} KERNELPATH=${STAGING_KERNEL_DIR} TOOLPREFIX=powerpc-linux-gnu- OS=linux KERNELARCH=powerpc TARGETARCH=powerpc-be-elf BOARD_TYPE=pb9x-2.6.31 athr-wpa_supplicant

	CFLAGS='-mcpu=8548 -mspe=yes -mabi=spe -mhard-float -mfloat-gprs=double' LDFLAGS='${LDFLAGS}' make INSTALL_ROOT=${D} KERNELPATH=${STAGING_KERNEL_DIR} TOOLPREFIX=powerpc-linux-gnu- OS=linux KERNELARCH=powerpc TARGETARCH=powerpc-be-elf BOARD_TYPE=pb9x-2.6.31 athr-hostapd

 	make CFLAGS='-mcpu=8548 -mspe=yes -mabi=spe -mhard-float -mfloat-gprs=double' LDFLAGS='${LDFLAGS}' INSTALL_ROOT=${D} KERNELPATH=${STAGING_KERNEL_DIR} TOOLPREFIX=powerpc-linux-gnu- OS=linux KERNELARCH=powerpc TARGETARCH=powerpc-be-elf BOARD_TYPE=pb9x-2.6.31 wireless_tools_main

	make LDFLAGS= INSTALL_ROOT=${D} KERNELPATH=${STAGING_KERNEL_DIR} TOOLPREFIX=powerpc-linux-gnu- OS=linux KERNELARCH=powerpc TARGETARCH=powerpc-be-elf BOARD_TYPE=pb9x-2.6.31 driver_build

	mv ${D}/etc/ath ${D}
	mv ${D}/etc/rc.d ${D}

	rm -rf ${D}/etc/*

	mv ${D}/ath ${D}/etc
	mv ${D}/rc.d ${D}/etc

	install -d -m0755 ${D}/usr/sbin
	ln -s /sbin/hostapd ${D}/usr/sbin/hostapd
}

do_install() {
}

FILES_${PN} += "${base_libdir}/*.so.*"
FILES_${PN} += "${base_sbindir}/*"
FILES_${PN} += "/usr/www/*"
FILES_${PN} += "/usr/styleSheet.css"
FILES_${PN} += "/bin/cfg"
FILES_${PN} += "/usr/www.single_ABG/APRadioConfig.html"
FILES_${PN} += "/usr/sbin/hostapd"


