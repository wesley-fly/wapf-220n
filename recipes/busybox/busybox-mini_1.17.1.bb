require recipes/busybox/busybox_${PV}.bb

FILESPATHPKG .= ":busybox-${PV}"

SRC_URI += "file://defconfig-minimal"

S = "${WORKDIR}/busybox-${PV}"

do_configure_prepend () {
	cp ${WORKDIR}/defconfig-minimal ${WORKDIR}/defconfig
}
