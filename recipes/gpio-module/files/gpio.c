#include <linux/kernel.h>
#include <linux/module.h>
#include <linux/signal.h>
#include <linux/interrupt.h>
#include <linux/irq.h>
#include <linux/init.h>
#include <linux/resource.h>
#include <linux/proc_fs.h>
#include <linux/miscdevice.h>
#include <linux/poll.h>
#include <asm/types.h>
#include <asm/irq.h>
#include <asm/delay.h>
#include <asm/system.h>
#include <linux/gpio.h>
#include <asm/io.h>

MODULE_DESCRIPTION("status_led and reset irq");
MODULE_AUTHOR("xa.Browan");
MODULE_LICENSE("GPL");

#define procfs_name "status_led"
struct proc_dir_entry *status_led;

static int status_led_state = 0;
static volatile int mpc85xx_fr_opened = 0;
unsigned int reset_mask = 0;
static wait_queue_head_t wapa_reset_wq;
//volatile unsigned int virt;
//volatile unsigned int *GPDIR, *GPDAT, *GPIER, *GPIMR, *GPICR;
int virq = 0;

int procfile_read(char *buffer,
	      char **buffer_location,
	      off_t offset, int buffer_length, int *eof, void *data)
{
	return sprintf(buffer, "%d\n", (status_led_state & 0x1) ? 1 : 0);
}

int procfile_write(struct file *file, const char *buffer, unsigned long count,
		   void *data)
{
	u_int32_t val;

	if (sscanf(buffer, "%d", &val) != 1)
		return -EINVAL;
	gpio_request(234,"status_led");
	if (val == 0) {
               status_led_state &= ~(0x1);
               gpio_direction_output(234,0);
        }
        else if (val == 1) {
               status_led_state |= 0x1;
               gpio_direction_output(234,1);
        }
        else {
                printk("%s: not accepted value, valid 0~1.\n", __func__);
        }

        return count;
}

irqreturn_t reset_key_irq(int irq, void *dev_id)
{
	local_irq_disable();
	unsigned int delay;
#define UDELAY3_COUNT   (20 * 1000)          // 20 seconds, for other
#define UDELAY4_COUNT   (10 * 1000)          // 10 seconds, for factory reset
#define UDELAY1_COUNT   (5 * 1000)             // 5 seconds, for wps 
#define UDELAY2_COUNT   (2 * 1000)             //  2 seconds, for reset board(reboot) 
        for (delay = UDELAY3_COUNT; delay; delay--) {
	    if (delay == UDELAY3_COUNT - 1)
       	        printk("SW2 button was down\n");
	    if (gpio_get_value(235)) {
		if (delay != UDELAY3_COUNT)
                    printk("SW2 button was up\n");
		break;
            }
            udelay(1000);
        }
	//printk("delay=%d\n",delay);
	if(!delay) {
                //printk("RESET pressed more than 20 seconds, for later use.\n");
		reset_mask = POLLERR;
                wake_up(&wapa_reset_wq);
		//local_irq_enable();
                //return IRQ_HANDLED;
	} else if (delay < (UDELAY3_COUNT - UDELAY4_COUNT)) {
                reset_mask = POLLPRI | POLLRDNORM;
                wake_up(&wapa_reset_wq);
	} else if (delay < ( UDELAY3_COUNT - UDELAY1_COUNT)) {
                //printk("RESET pressed for 5 seconds, now clear configurations.\n");
		reset_mask = POLLIN | POLLRDNORM;
	        wake_up(&wapa_reset_wq);
		//local_irq_enable();
                //return IRQ_HANDLED;
	} else if (delay < (UDELAY3_COUNT - UDELAY2_COUNT)) {
                //printk("RESET pressed for 2 seconds, now going reboot board.\n");
		reset_mask = POLLOUT | POLLRDNORM;
            	wake_up(&wapa_reset_wq);
		//local_irq_enable();
                //return IRQ_HANDLED;
	} else {
		//printk("RESET is pressed, but no actions.\n");
		//local_irq_enable();
                //return IRQ_HANDLED;
	}
	//gpio_set_value(235,1)	
	//printk(KERN_INFO "gpio_get_value(235)=%d,gpio_to_irq(235)=%d\n",gpio_get_value(235),gpio_to_irq(235));	
	/**GPIER |= 1 < 20;
        udelay(10);
	*GPDAT &= 1 < 21;
        udelay(10);*/
	//printk("before return\n");
	//udelay(500000);
	local_irq_enable();
	return IRQ_HANDLED;
}

static int mpc85xxfr_open(struct inode *inode, struct file *file)
{
        if (MINOR(inode->i_rdev) != FACTORY_RESET_MINOR) {
                return -ENODEV;
        }

        if (mpc85xx_fr_opened) {
                return -EBUSY;
        }

        mpc85xx_fr_opened = 1;
        return nonseekable_open(inode, file);
}

static int mpc85xxfr_close(struct inode *inode, struct file *file)
{
        if (MINOR(inode->i_rdev) != FACTORY_RESET_MINOR) {
                return -ENODEV;
        }

        mpc85xx_fr_opened = 0;
        return 0;
}

static ssize_t mpc85xxfr_read(struct file *file, char *buf, size_t count, loff_t *ppos)
{
        return -ENOTSUPP;
}

static ssize_t mpc85xxfr_write(struct file *file, const char *buf, size_t count, loff_t *ppos)
{
        return -ENOTSUPP;
}

static unsigned int mpc85xxfr_poll(struct file *filp, poll_table *wait)
{
        unsigned int bmask = reset_mask;
        //atomic_inc(&wapa_reset_status);
        poll_wait(filp, &wapa_reset_wq, wait);
        return (reset_mask=0, bmask);
}

static struct file_operations mpc85xxfr_fops = {
        read:   mpc85xxfr_read,
        write:  mpc85xxfr_write,
        open:   mpc85xxfr_open,
        release:mpc85xxfr_close,
        poll:   mpc85xxfr_poll
};

static struct miscdevice mpc85xxfr_miscdev =
{ FACTORY_RESET_MINOR, "Factory reset", &mpc85xxfr_fops };


int init_module()
{
	int req,ret;

	init_waitqueue_head(&wapa_reset_wq);

	ret = misc_register(&mpc85xxfr_miscdev);
	if (ret < 0) {
        	printk(KERN_ERR "*** mpc85xx misc_register failed %d *** \n", ret);
        	return -1;
	}

	status_led = create_proc_entry(procfs_name, 0644, NULL);	
	if (status_led == NULL) {
		remove_proc_entry(procfs_name, NULL);
		printk(KERN_ALERT "Error: Could not initialize /proc/%s\n",
		       procfs_name);
		return -ENOMEM;
	}

	status_led->read_proc = procfile_read;
	status_led->write_proc = procfile_write;
	status_led->mode 	 = S_IFREG | S_IRUGO;
	status_led->uid 	 = 0;
	status_led->gid 	 = 0;
	status_led->size 	 = 37;

	//printk(KERN_INFO "/proc/%s created\n", procfs_name);	
	//req = request_irq(gpio_to_irq(235), reset_key_irq, 0, "reset_key", NULL);
	//printk(KERN_INFO "gpio_get_value(235)=%d,gpio_to_irq(235)=%d\n",gpio_get_value(235),gpio_to_irq(235));
	//np = of_find_node_by_name(NULL, "global-utilities");

	/*virt = ioremap(0xffe0f000,0x100);
	printk("virt = %x\n", virt);
	GPDIR = (volatile unsigned int *)(virt + 0x00);
	GPDAT = (volatile unsigned int *)(virt + 0x08);
	GPIER = (volatile unsigned int *)(virt + 0x0c);
	GPIMR = (volatile unsigned int *)(virt + 0x10);
	GPICR = (volatile unsigned int *)(virt + 0x14);
	
	*GPDIR &= 0 < 20;
	udelay(10);
	*GPDAT &= 0 < 21;
	udelay(10);
	*GPIMR |= 1 < 20;//0x00100000;
        udelay(10);
	*GPICR &= 1 < 20;
        udelay(10);
	req = request_irq(gpio_to_irq(235), reset_key_irq, 0, "reset_key", NULL);*/
	//gpio_request(235,"reset key");
	//gpio_direction_input(235);
	virq = irq_create_mapping(NULL,11);
	if (virq == NO_IRQ) 
		printk(KERN_ERR "irq_create_mapping() couldn't create irq mapping for IRQ_11");
	req = request_irq(virq, reset_key_irq, 0, "reset_key", NULL);
	if (req)
		printk(KERN_ERR "request_irq() couldn't reserve irq (error %d)\n", req);
	set_irq_type(virq, IRQ_TYPE_EDGE_FALLING); 
	return 0;
}

void cleanup_module()
{
	remove_proc_entry(procfs_name, NULL);
	//printk(KERN_INFO "/proc/%s removed\n", procfs_name);
	free_irq(virq,NULL);
	//iounmap((volatile void *)0xffe0f000);
}

