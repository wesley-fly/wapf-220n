# Common things we want to do in all System Builder images

EXTRA_IMAGECMD_jffs2 = "${JFFS2_ARGS} && sumtool ${JFFS2_ARGS} -i ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.jffs2 -o ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.jffs2.summary && mv ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.jffs2.summary ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.jffs2"

# We need to delay the deletion
REAL_IMAGE_KEEPROOTFS := ${IMAGE_KEEPROOTFS}
IMAGE_KEEPROOTFS = "1"

python do_print_ramdisk_msg() {
    if not '.u-boot' in bb.data.expand("${IMAGE_FSTYPES}", d):
        return
    sz = oe.process.run(bb.data.expand("du -ks ${IMAGE_ROOTFS}|awk '{size = ${IMAGE_EXTRA_SPACE} + $1; print (size > ${IMAGE_ROOTFS_SIZE} ? size : ${IMAGE_ROOTFS_SIZE}) }'", d))
    bb.note("If you are using this image as a ramdisk you may need to pass 'ramdisk_size=%dk' in your kernel arguements.  In U-Boot for example:\nsetenv bootargs root=/dev/ram rw ramdisk_size=%dk" % (int(sz), int(sz)))
    if not bool(bb.data.expand("REAL_IMAGE_KEEPROOTFS", d)):
        oe.process.ruin(bb.data.expand("rm -rf ${IMAGE_ROOTFS}", d))
}

addtask print_ramdisk_msg before do_build after do_rootfs
do_print_ramdisk_msg[nostamp] = "1"
