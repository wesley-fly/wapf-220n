# There are two U-Boot settings in this file, one is SPIFLASH, the other is NAND
# They should not be enabled at the same time.

# SPI FLASH BOOT
UBOOT_MACHINE="P1010RDB_SPIFLASH_config"

# NAND BOOT
#UBOOT_MACHINE="P1010RDB_NAND_config"

SRC_URI += "file://001_U-BOOT.patch"

SRC_URI += "file://003_support_mx25l12835f.patch"
# FOR INTERNAL SPI FLASH TEST ONLY
#SRC_URI += "file://002_CMD_SFTEST.patch"

