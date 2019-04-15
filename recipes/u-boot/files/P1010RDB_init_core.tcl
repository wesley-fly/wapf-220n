#########################################################################
# Initialization file for P1010RDB board
# Clock Configuration:
#       CPU: 800 MHz, CCB: 400 MHz,
#       DDR: 667 MHz, SYSCLK:  66 MHz
#########################################################################
variable CCSRBAR 0xff700000

proc CCSR {reg_off} {
	global CCSRBAR
	return p:0x[format %x [expr {$CCSRBAR + $reg_off}]]
}

proc apply_e500v2_workaround {} {
	# After reset, e500 cores need to run before being able to enter debug mode.
	# Work-around: set a HW BP at reset address and run the core; after the core hits the BP, it enters debug mode
	# e500 cores need to have valid opcode at the interrupt vector
	variable SPR_GROUP "e500 Special Purpose Registers/"	
	#######################################################################
	# Set a breakpoint at the reset address 
	reg ${SPR_GROUP}IAC1 = 0xfffffffc
	reg ${SPR_GROUP}DBCR0 = 0x40800000
	reg ${SPR_GROUP}DBCR1 = 0x00000000

	# Run the core
	config runcontrolsync off
	go
	wait 50   
	config runcontrolsync on
	stop

	# Clear affected registers 	
	reg ${SPR_GROUP}DBSR  = 0x01CF0000
	reg ${SPR_GROUP}DBCR0 = 0x41000000
	reg ${SPR_GROUP}IAC1  = 0x00000000
	reg ${SPR_GROUP}CSRR0 = 0x00000000
	reg ${SPR_GROUP}CSRR1 = 0x00000000
}



proc init_P1010 {} {
	global CCSRBAR
	variable SPR "e500 Special Purpose Registers/"
	variable SSPR "Standard Special Purpose Registers/"
	variable GPRS "General Purpose Registers/"

	# get ROM_LOC from PORBMSR
	variable ROM_LOC 					0x[format %x [expr {([mem [CCSR 0xE0004] -np] & 0x0f000000) >> 24}]]

	######################################################################
	# move CCSR at 0xE0000000
	# CCSRBAR
	# bit 8 - 23 - BASE_ADDR
	mem [CCSR 0x0] = 0x000e0000
	set CCSRBAR 0xe0000000

	######################################################################
	# invalidate BR0
	# CSPR0
	mem [CCSR 0x1E010] = 0x00000100

	# ABIST off
	# L2ERRDIS[MBECCDIS]=1 L2ERRDIR[SBECCDIS]=1
	mem [CCSR 0x20E44] = 0x0000000C

	# activate debug interrupt and enable SPU
	reg ${SSPR}MSR = 0x02000200

	######################################################################
	#	
	#	Memory Map
	#
	#   0x00000000  0x3FFFFFFF 	DDR	                1GB
	#   0x80000000  0xBFFFFFFF  PEX           		1GB
	#   0xE0000000	0xE00FFFFF	CCSRBAR Space	    1M
	#   0xEFA00000	0xEFA03FFF  NAND			    16k
	#   0xEFB00000	0xEFBFFFFF  Board CPLD          1M
	#   0xEFC00000  0xEFC0FFFF  PEX I/O 			256k
	#   0xFE000000	0xFFFFFFFF  LocalBus NOR FLASH	32M
	#
	######################################################################

	variable CAM_GROUP "regPPCTLB1/"
	# MMU  initialization
	# define 1MB    TLB1 entry 2: 0xE0000000 - 0xE00FFFFF; for CCSR Space, cache inhibited, guarded
	reg ${CAM_GROUP}L2MMU_CAM2 = 0x500003CA1C080000E0000000E0000001

	# define 16MB   TLB1 entry 3: 0xFE000000 - 0xFEFFFFFF; for Local Bus, cache inhibited, guarded
	reg ${CAM_GROUP}L2MMU_CAM3 = 0x70000FCA1C080000FE000000FE000001

	# define 16MB   TLB1 entry 4: 0xFF000000 - 0xFFFFFFFF; for Local Bus, cache inhibited, guarded
	reg ${CAM_GROUP}L2MMU_CAM4 = 0x70000FCA1C080000FF000000FF000001

	# define 1GB    TLB1 entry 5: 0x80000000 - 0xBFFFFFFF; for PCI Express, cache inhibited, guarded
	reg ${CAM_GROUP}L2MMU_CAM5 = 0xA0007FCA1C0800008000000080000001


	# define 256KB  TLB1 entry 6: 0xEFC00000 - 0xEFC3FFFF; for PCI Express I/O, cache inhibited, guarded
	reg ${CAM_GROUP}L2MMU_CAM6 = 0x400001CA1C080000EFC00000EFC00001

	# define 256KB  TLB1 entry 7: 0xEFB00000 - 0xEFB3FFFF; for CPLD, cache inhibited, guarded
	reg ${CAM_GROUP}L2MMU_CAM7 = 0x400001CA1C080000EFB00000EFB00001
	# define 1GB    TLB1 entry 8: 0x00000000 - 0x3FFFFFFF; DDR
	reg ${CAM_GROUP}L2MMU_CAM8 = 0xA0007FC01C0800000000000000000001
	# define 16k    TLB1 entry 9: 0xEFA00000 - 0xEFA03FFF; for NAND cache inhibited, guarded
	reg ${CAM_GROUP}L2MMU_CAM9 = 0x2000004A1C080000EFA00000EFA00001


	#delete CAM0
	reg ${CAM_GROUP}L2MMU_CAM0 = 0x00000000000000000000000000000000
	#delete CAM1
	reg ${CAM_GROUP}L2MMU_CAM1 = 0x00000000000000000000000000000000
	#delete CAM10
	reg ${CAM_GROUP}L2MMU_CAM10 = 0x00000000000000000000000000000000
	#delete CAM11
	reg ${CAM_GROUP}L2MMU_CAM11 = 0x00000000000000000000000000000000
	#delete CAM12
	reg ${CAM_GROUP}L2MMU_CAM12 = 0x00000000000000000000000000000000
	#delete CAM13
	reg ${CAM_GROUP}L2MMU_CAM13 = 0x00000000000000000000000000000000
	#delete CAM14
	reg ${CAM_GROUP}L2MMU_CAM14 = 0x00000000000000000000000000000000
	#delete CAM15
	reg ${CAM_GROUP}L2MMU_CAM15 = 0x00000000000000000000000000000000

	######################################################################
	#
	#	Memory Windows
	#
	#	0xFE000000	0xFFFFFFFF	LAW0	NOR FLASH - 32M
	#   0xEFB00000  0xEFBFFFFF  LAW1    CPLD - 128K
	#   0x80000000  0x9FFFFFFF  LAW2    PEX2  - 512M
	#   0xEFC30000  0xEFC3FFFF  LAW3    PEX2  - 64K
	#   0xA0000000  0xBFFFFFFF  LAW4    PEX1  - 512M
	#   0xEFC20000  0xEFC2FFFF  LAW5    PEX1  - 64K
	#   0xEFA00000  0xEFAFFFFF  LAW6    NAND  - 16k
	#	0x00000000	0x3FFFFFFF	LAW11	DDRC - 1G
	#
	#####################################################################
	# configure local access windows
	# LAWBAR0 - Local Bus
	# bit 8 - 31 = 0xFE000000 - base addr
	mem [CCSR 0xC08] = 0x000fe000
	# LAWAR0
	# bit 0 = 1 - enable window
	# bit 7-11 = 00100 - Local Bus
	# bit 26 - 31 =  011011 32M - size
	mem [CCSR 0xC10] = 0x80400018

	# LAWBAR1 - CPLD
	# bit 8 - 31 = 0xEFB00000 - base addr
	mem [CCSR 0xC28] = 0x000efb00

	# LAWAR1
	# bit 0 = 1 - enable window
	# bit 7-11 = 00100 - Local Bus
	# bit 26-31 = 010011 128k - size
	mem [CCSR 0xC30] = 0x80400010

	# LAWBAR2 - PEX2
	# bit 8 - 31 = 0x80000000 - base addr
	mem [CCSR 0xC48] = 0x00080000

	# LAWAR2
	# bit 0 = 1 - enable window
	# bit 7-11 = 00001 - PEX2
	# bit 26 - 31 =  011100 512M - size
	mem [CCSR 0xC50] = 0x8020001c

	# LAWBAR3 - PEX 2 I/O
	# bit 8 - 31 = 0xEFC300000 - base addr
	mem [CCSR 0xC68] = 0x000efc30

	# LAWAR3
	# bit 0 = 1 - enable window
	# bit 7-11 = 00010 - PEX2
	# bit 26-31 = 001111 64k - size
	mem [CCSR 0xC70] = 0x8020000f

	# LAWBAR4 - PEX 1
	# bit 8 - 31 = 0xA0000000 - base addr
	mem [CCSR 0xC88] = 0x000a0000

	# LAWAR4
	# bit 0 = 1 - enable window
	# bit 7-11 = 00010 - PEX1
	# bit 26 - 31 =  011100 512M - size
	mem [CCSR 0xC90] = 0x8010001c

	# LAWBAR5 - PEX 1 I/O
	# bit 8 - 31 = 0xEFC200000 - base addr
	mem [CCSR 0xCA8] = 0x000efc20

	# LAWAR5
	# bit 0 = 1 - enable window
	# bit 7-11 = 00001 - PEX1
	# bit 26-31 = 001111 64k - size
	mem [CCSR 0xCB0] = 0x8010000f

	# LAWBAR6 - Local Bus
	# bit 8 - 31 = 0xEFA00000 - base addr
	mem [CCSR 0xCC8] = 0x00efa00

	# LAWAR6
	# bit 0 = 1 - enable window
	# bit 7-11 = 00100 - IFC
	# bit 26-31 = 010011 16k - size
	#mem [CCSR 0xCD0] = 0x8040000d
	mem [CCSR 0xCD0] = 0x8040001B
	# LAWBAR11 - DDR
	# bit 8 - 31 = 0x00000000 - base addr
	mem [CCSR 0xD68] = 0x00000000
	# LAWAR11
	# bit 0 = 1 - enable window
	# bit 7-11 = 01111 - DDR
	# bit 26 - 31 =  011100 512M - size
	mem [CCSR 0xD70] = 0x80F0001C

	#disable LAW 7
	mem [CCSR 0xCE8] = 0x00000000
	mem [CCSR 0xCF0] = 0x00000000
	#disable LAW 8
	mem [CCSR 0xD08] = 0x00000000
	mem [CCSR 0xD10] = 0x00000000
	#disable LAW 9
	mem [CCSR 0xD28] = 0x00000000
	mem [CCSR 0xD30] = 0x00000000
	#disable LAW 10
	mem [CCSR 0xD48] = 0x00000000
	mem [CCSR 0xD50] = 0x00000000


	#######################################
	#	CPLD INIT
	#######################################
	# we don't need CPLD
	#config MemAccess 8
	#config MemWidth 8
	#mem v:0xefb0000a = 0x00
	#mem v:0xefb00009 = 0x00
	#mem v:0xefb00013 = 0x00
	#config MemAccess 32
	#config MemWidth 32
	#######################################
	#	DDRC INITIALIZATION
	#######################################

	# DDR_SDRAM_CFG
	mem [CCSR 0x2110] = 0x47140000
	# CS0_BNDS
	# 0x0000-0x001F
	mem [CCSR 0x2000] = 0x0000001F
	# CS1_BNDS
	# 0x0020-0x003F
	mem [CCSR 0x2008] = 0x0020003F
	# CS0_CONFIG
	mem [CCSR 0x2080] = 0x80014202
	# CS1_CONFIG
	mem [CCSR 0x2084] = 0x80014202
	# TIMING_CFG_0
	mem [CCSR 0x2104] = 0x00110004
	# TIMING_CFG_1
	mem [CCSR 0x2108] = 0x6f6b8644
	# TIMING_CFG_2
	mem [CCSR 0x210c] = 0x0fa888cf
	# TIMING_CFG_3
	mem [CCSR 0x2100] = 0x00030000
	# DDR_SDRAM_CFG_2
	mem [CCSR 0x2114] = 0x24401000
	# DDR_SDRAM_MODE
	mem [CCSR 0x2118] = 0x00441210
	# DDR_SDRAM_MODE_2
	mem [CCSR 0x211C] = 0x00000000
	# DDR_SDRAM_MD_CNTL
	mem [CCSR 0x2120] = 0x00000000
	# DDR_SDRAM_INTERVAL
	mem [CCSR 0x2124] = 0x0a280000
	# DDR_DATA_INIT
	mem [CCSR 0x2128] = 0xDEADBEEF
	# DDR_SDRAM_CLK_CNTL
	mem [CCSR 0x2130] = 0x03000000
	# TIMING_CFG_4
	mem [CCSR 0x2160] = 0x00000001
	# TIMING_CFG_5
	mem [CCSR 0x2164] = 0x03402400
	# DDR_ZQ_CNTL
	mem [CCSR 0x2170] = 0x89080600
	# DDR_WRLVL_CNTL
	mem [CCSR 0x2174] = 0x8655a608
	# ERR_INT_EN
	mem [CCSR 0x2E48] = 0x00000000
	# ERR_SBE
	mem [CCSR 0x2E58] = 0x00000000
	# DDRCDR_1
	mem [CCSR 0x2B28] = 0x00000000
	# DDRCDR_2
	mem [CCSR 0x2B2C] = 0x00000000
	#delay before enable
	wait 500
	# DDR_SDRAM_CFG
	mem [CCSR 0x2110] = 0xC7140000
	#wait for DRAM data initialization
	wait 500

	##################################################################################
	# added workaround for Errata ID - GEN-A016 (A-003549)
	# PMUXCR
	mem [CCSR 0xE0060] = 0x000000c0

	##################################################################################
	# configure IFC controller 
	# Autodetect CS routing based on ROM_LOC
	# bits 4-7 - ROM_LOC
	# 1000 8-bit NAND?12b page size
	# 1001 8-bit NAND?K page size
	# 1010 8-bit NAND?K page size
	# 1011 8-bit NOR
	# 1100 16-bit NAND?12b page size
	# 1101 16-bit NAND?K page size
	# 1110 16-bit NAND?K page size
	# 1111 16b NOR

	# boot location is NAND => NAND - cS0, NOR - CS1
	if {($ROM_LOC >= 0x8 && $ROM_LOC <= 0xA) || (($ROM_LOC >= 0xC && $ROM_LOC <= 0xE))} {
		###########################
		# CS0 - NAND Flash settings
		# AMASK0 64k NAND Flash buffer size
		mem [CCSR 0x1E0A0] = 0xFFFF0000
		# CSOR0
		mem [CCSR 0x1E130] = 0x00800000
		#FTIM0_CS0 
		mem [CCSR 0x1E1C0] = 0x020C0405
		#FTIM1_CS0
		mem [CCSR 0x1E1C4] = 0x1D1D070C
		#FTIM2_CS0
		mem [CCSR 0x1E1C8] = 0x0180280F
		#FTIM3_CS0
		mem [CCSR 0x1E1CC] = 0x04000000
		# CSPR0
		mem [CCSR 0x1E010] = 0xEFA00083
		##########################
		# CS1 - NOR Flash settings
		# AMASK1 32M NOR
		mem [CCSR 0x1E0AC] = 0xFE000000
		# CSOR1
		mem [CCSR 0x1E13C] = 0x0000E000
		#FTIM0_CS1
		mem [CCSR 0x1E1F0] = 0x40050005
		#FTIM1_CS1
		mem [CCSR 0x1E1F4] = 0x1e000f00
		#FTIM2_CS1
		mem [CCSR 0x1E1F8] = 0x0410001c
		#FTIM3_CS1
		mem [CCSR 0x1E1FC] = 0x00000000
		# CSPR1
		mem [CCSR 0x1E01C] = 0xFE000101 
	} else {
		##########################
		# CS0 - NOR Flash settings
		# AMASK0 32M NOR
		mem [CCSR 0x1E0A0] = 0xFE000000
		# CSOR0
		mem [CCSR 0x1E130] = 0x0000E000
		#FTIM0_CS0 
		mem [CCSR 0x1E1C0] = 0x40050005
		#FTIM1_CS0
		mem [CCSR 0x1E1C4] = 0x1e000f00
		#FTIM2_CS0
		mem [CCSR 0x1E1C8] = 0x410001c
		#FTIM3_CS0
		mem [CCSR 0x1E1CC] = 0x00000000
		# CSPR0
		mem [CCSR 0x1E010] = 0xFE000101 

		###########################
		# CS1 - NAND Flash settings
		# AMASK1 64k NAND Flash buffer size
		mem [CCSR 0x1E0AC] = 0xFFFF0000

		# CSOR1
		mem [CCSR 0x1E13C] = 0x00800000 

		#FTIM0_CS1
		mem [CCSR 0x1E1F0] = 0x020C0405
		#FTIM1_CS1
		mem [CCSR 0x1E1F4] = 0x1D1D070C
		#FTIM2_CS1
		mem [CCSR 0x1E1F8] = 0x0180280F
		#FTIM3_CS1
		mem [CCSR 0x1E1FC] = 0x04000000

		# CSPR1
		mem [CCSR 0x1E01C] = 0xEFA00083
	}

	# CS3 - CPLD
	#CSPR3 base address at 0xEFB00000, valid
	mem [CCSR 0x1E034] = 0xEFB00085
	#AMASK3 64K 
	mem [CCSR 0x1E0C4] = 0xFFFF0000
	#CSOR3
	mem [CCSR 0x1E154] = 0x00000000

	#FTIM0_CS3 
	mem [CCSR 0x1E250] = 0xe00e000e
	#FTIM1_CS3
	mem [CCSR 0x1E254] = 0x0e001f00
	#FTIM2_CS3

	mem [CCSR 0x1E258] = 0x0e00001f

	#FTIM3_CS3
	mem [CCSR 0x1E25C] = 0x00000000
	#GCR
	mem [CCSR 0x1E40C] = 0x0e008000
	#CCR
	mem [CCSR 0x1E44C] = 0x00000800

	#SPI init
	# SPMODE 
	mem [CCSR 0x7000] = 0x80000403 
	# SPIM - catch all events
	mem [CCSR 0x7008] = 0x0000FB00
	# SPMODE1
	mem [CCSR 0x7020] = 0x28170008

	####################################################################
	# interrupt vectors initialization
	# IVPR (default reset value) 
	reg ${SPR}IVPR = 0x00000000
	# interrupt vector offset registers
	# IVOR0 - critical input
	reg	${SPR}IVOR0 = 0x00000100
	# IVOR1 - machine check
	reg	${SPR}IVOR1 = 0x00000200
	# IVOR2 - data storage
	reg	${SPR}IVOR2 = 0x00000300
	# IVOR3 - instruction storage
	reg	${SPR}IVOR3 = 0x00000400
	# IVOR4 - external input
	reg	${SPR}IVOR4 = 0x00000500
	# IVOR5 - alignment
	reg	${SPR}IVOR5 = 0x00000600
	# IVOR6 - program
	reg	${SPR}IVOR6 = 0x00000700
	# IVOR8 - system call
	reg	${SPR}IVOR8 = 0x00000c00
	# IVOR10 - decrementer
	reg	${SPR}IVOR10 = 0x00000900
	# IVOR11 - fixed-interval timer interrupt
	reg	${SPR}IVOR11 = 0x00000f00
	# IVOR12 - watchdog timer interrupt
	reg	${SPR}IVOR12 = 0x00000b00
	# IVOR13 - data TLB errror
	reg	${SPR}IVOR13 = 0x00001100
	# IVOR14 - instruction TLB error
	reg	${SPR}IVOR14 = 0x00001000
	# IVOR15 - debug
	reg	${SPR}IVOR15 = 0x00001500
	# IVOR32 - SPE-APU unavailable
	reg	${SPR}IVOR32 = 0x00001600
	# IVOR33 - SPE-floating point data exception
	reg	${SPR}IVOR33 = 0x00001700
	# IVOR34 - SPE-floating point round exception
	reg	${SPR}IVOR34 = 0x00001800
	# IVOR35 - performance monitor
	reg	${SPR}IVOR35 = 0x00001900

	# put a valid opcode at debug and progrm exception vector address
	mem v:0x00000700 = 0x48000000
	mem v:0x00001500 = 0x48000000

	##################################################################################
	apply_e500v2_workaround	
	#############
	#
	# enable floating point
	reg ${SSPR}MSR = 0x02001200
	############
	#
	# time base enable & MAS7 update
	# HID0
	reg	${SPR}HID0 = 0x00004080
	######
	# CW debugger settings
	#
	#Trap debug event enable
	reg	${SPR}DBCR0 = 0x41000000
	# for debugging starting at program entry point when stack is not initialized
	reg	${GPRS}SP = 0x0000000F
}

proc envsetup {} {
	# Environment Setup
	radix x 
	config hexprefix 0x
	config MemIdentifier v
	config MemWidth 32 
	config MemAccess 32 
	config MemSwap off
}

#-------------------------------------------------------------------------------
# Main                                                                          
#-------------------------------------------------------------------------------
  envsetup

  init_P1010