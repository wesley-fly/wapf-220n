DESCRIPTION = "luci"
PR = "r0"
SRC_URI = "file://${PN}-${PV}.tar.gz \
	   file://001_associated.patch \
	   file://002_for_fwinfo.patch "

DEPENDS += "openssl"
DEPENDS += "lua"

S = "${WORKDIR}/${PN}-${PV}"

INITSCRIPT_NAME = "luci"

inherit pkgconfig binconfig

LUCI_MODULES="libs/core libs/ipkg libs/lmo libs/nixio libs/sys libs/web protocols/core protocols/ppp modules/admin-core modules/admin-full applications/luci-firewall libs/sgi-cgi themes/base themes/openwrt i18n/english"

PARALLEL_MAKE = "-j 1"
EXTRA_OEMAKE = "'CC=${CC}' 'MODULES=${LUCI_MODULES}'  'OS="Linux"'"

do_compile() {
	oe_runmake
}

do_install() {
	for i in ${LUCI_MODULES}; do cp -pR $i/dist/* ${D} ; done
}

FILES_${PN} += "/lib/upgrade/luci-add-conffiles.sh"
FILES_${PN} += "/usr/*"
FILES_${PN} += "/www/*"
