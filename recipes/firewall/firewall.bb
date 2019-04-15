DESCRIPTION = "firewall files"
PR = "r0"
SRC_URI = "file://${PN}.tar.gz"

S = "${WORKDIR}/${PN}"

do_compile() {
}

do_install() {
	cp -a * ${D}
}

FILES_${PN} += "/lib/firewall/*"

