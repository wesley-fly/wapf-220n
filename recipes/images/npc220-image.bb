# Busybox-only image.
# Allows login via serial console

inherit image

RPROVIDES = "npc220-image"
IMAGE_INSTALL = "uci busybox-mini base-files art-tools atheros-sdk iptables dropbear hotplug2 iwinfo udevtrigger uhttpd firewall dnsmasq ppp gpio-module wled watchdog-module lua luci i2c-tools"
#IMAGE_INSTALL = "busybox-mini"

export PACKAGE_INSTALL = "${IMAGE_INSTALL} ppp-oe"
IMAGE_LINGUAS = ""

DEPENDS = "${IMAGE_INSTALL}"
RDEPENDS = "${IMAGE_INSTALL}"

# No package management here
ONLINE_PACKAGE_MANAGEMENT = "none"

# We do it all with busybox-minimal.
IMAGE_DEV_MANAGER = "busybox-minimal"
IMAGE_INIT_MANAGER = "busybox-minimal"
IMAGE_INITSCRIPTS = "busybox-minimal"
IMAGE_LOGIN_MANAGER = "busybox-minimal"

require sb-image-common.inc

IMAGE_DEVICE_TABLES = "recipes/images/files/device_table-minimal.txt"

busybox_rootfs_prep () {
#        cat <<EOF > ${IMAGE_ROOTFS}/etc/profile
# for test
#export PATH=\$PATH:/usr/local/sbin:/tmp/tools:/etc/ath

#EOF
        #chmod 755 ${IMAGE_ROOTFS}/etc/profile

	# Make some directories
	mkdir ${IMAGE_ROOTFS}/{mnt,proc,sys,tmp,root,overlay}
	cd ${IMAGE_ROOTFS}/../deploy/glibc/images/ && tar zcvf WAPF-220N_FW.tar.gz p1010rdb.dtb uImage-p1010rdb.bin npc220-image-p1010rdb.jffs2
	#modify inittab
	[ -s  ${IMAGE_ROOTFS}/etc/inittab ] && sed -i '/tts/d' ${IMAGE_ROOTFS}/etc/inittab

	# Make mtab a symlink.
	#ln -s /proc/mtab ${IMAGE_ROOTFS}/etc/mtab
	
	# Make /var/run a symlink
	rm -rf ${IMAGE_ROOTFS}/var
	ln -s /tmp ${IMAGE_ROOTFS}/var

	# Make i2c node
	#install -d ${IMAGE_ROOTFS}/dev
	#mknod ${IMAGE_ROOTFS}/dev/i2c-0 c 89 0
	#mknod ${IMAGE_ROOTFS}/dev/i2c-1 c 89 1
	# Make watchdog node
	#mknod ${IMAGE_ROOTFS}/dev/watchdog c 10 130
	# Make nvram node
	#mknod ${IMAGE_ROOTFS}/dev/caldata b 31 6

	#mknod ${IMAGE_ROOTFS}/dev/ttyS0 c 4 64
	
	rm -rf ${IMAGE_ROOTFS}/boot/

	mkdir ${IMAGE_ROOTFS}/etc/crontabs
	mkdir ${IMAGE_ROOTFS}/etc/wpa2

	# Modify for wpa
	#mv ${IMAGE_ROOTFS}/dev/random ${IMAGE_ROOTFS}/dev/random.org
	#ln -s /dev/urandom ${IMAGE_ROOTFS}/dev/random
}

IMAGE_PREPROCESS_COMMAND += "busybox_rootfs_prep;"
