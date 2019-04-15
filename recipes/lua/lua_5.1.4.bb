DESCRIPTION = "lua"
PR = "r0"
SRC_URI = "file://${PN}-${PV}.tar.gz \
           file://010-lua-5.1.3-lnum-full-260308.patch \
           file://011-lnum-use-double.patch \
           file://015-lnum-ppc-compat.patch \
           file://020-shared_liblua.patch \
           file://030-archindependent-bytecode.patch \
           file://100-no_readline.patch \
           file://200-lua-path.patch \
           file://300-opcode_performance.patch \
           file://400-luaposix_5.1.4-embedded.patch \
           file://500-eglibc_config.patch \
	   file://600_timezone.patch"

DEPENDS += "readline"

S = "${WORKDIR}/lua-${PV}"

INITSCRIPT_NAME = "lua"

inherit pkgconfig binconfig

GET_CC_ARCH += " -fPIC ${LDFLAGS}"
EXTRA_OEMAKE = "'CC=${CC} -fPIC' 'MYCFLAGS=${CFLAGS} -DLUA_USE_LINUX -fPIC' MYLDFLAGS='${LDFLAGS}' PKG_VERSION=5.1.4"

do_compile() {
	oe_runmake linux
}

do_install() {
	oe_runmake \
		'INSTALL_TOP=${D}${prefix}' \
		'INSTALL_BIN=${D}${bindir}' \
		'INSTALL_INC=${D}${includedir}/' \
		'INSTALL_MAN=${D}${mandir}/man1' \
		'INSTALL_SHARE=${D}${datadir}/lua' \
		install
}

