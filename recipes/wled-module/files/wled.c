#include <linux/module.h>	/* Specifically, a module */
#include <linux/kernel.h>	/* We're doing kernel work */
#include <linux/proc_fs.h>	/* Necessary because we use the proc fs */
#include <linux/pci.h>

MODULE_DESCRIPTION("wlan 2/5G leds");
MODULE_AUTHOR("xa.Browan");
MODULE_LICENSE("GPL");

#define name_5g "WL_5G_LED"
#define name_2g "WL_2G_LED"

struct proc_dir_entry *procfile_5g;
struct proc_dir_entry *procfile_2g;
static int led_state_5g = 1;
static int led_state_2g = 1;
void __iomem *g_ioaddr_5g;
void __iomem *g_ioaddr_2g;

int procfile_read_5g(char *buffer, char **buffer_location, off_t offset, int buffer_length, int *eof, void *data)
{
        return sprintf(buffer, "%d\n", (led_state_5g & 0x1) ? 1 : 0);
}
int procfile_write_5g(struct file *file, const char *buffer, unsigned long count, void *data)
{
        u_int32_t val;

        if (sscanf(buffer, "%d", &val) != 1)
                return -EINVAL;
        if (val == 0) {
                led_state_5g &= ~(0x1);
		iowrite32((ioread32(g_ioaddr_5g + 0x4048) & ~(1u << 10)), (g_ioaddr_5g + 0x4048));
		//printk("5g write 0 to %lX,read value = %lX, write value = %lX\n",(g_ioaddr_5g + 0x4048), ioread32(g_ioaddr_5g + 0x4048),(ioread32(g_ioaddr_5g + 0x4048) & ~(0x1 << 10)));
        }
        else if (val == 1) {
                led_state_5g |= 0x1;
		iowrite32((ioread32(g_ioaddr_5g + 0x4048) | (1u << 10)), (g_ioaddr_5g + 0x4048));
        }
        else {
                printk("%s: not accepted value, valid 0~1.\n", __func__);
        }

        return count;
}

int procfile_read_2g(char *buffer, char **buffer_location, off_t offset, int buffer_length, int *eof, void *data)
{
        return sprintf(buffer, "%d\n", (led_state_2g & 0x1) ? 1 : 0);
}
int procfile_write_2g(struct file *file, const char *buffer, unsigned long count, void *data)
{
        u_int32_t val;

        if (sscanf(buffer, "%d", &val) != 1)
                return -EINVAL;
        if (val == 0) {
                led_state_2g &= ~(0x1);
                iowrite32((ioread32(g_ioaddr_2g + 0x4048) & ~(1u << 10)), (g_ioaddr_2g + 0x4048));
        }
        else if (val == 1) {
                led_state_2g |= 0x1;
                iowrite32((ioread32(g_ioaddr_2g + 0x4048) | (1u << 10)), (g_ioaddr_2g + 0x4048));
        }
        else {
                printk("%s: not accepted value, valid 0~1.\n", __func__);
        }

        return count;
}
int init_module()
{
	unsigned long mmio_start_5g, mmio_start_2g, mmio_len;

	mmio_start_5g = 0xa0000000;
	mmio_start_2g = 0x80000000;
	mmio_len = 0x10000;

        procfile_5g = create_proc_entry(name_5g, 0644, NULL);
        if (procfile_5g == NULL) {
                remove_proc_entry(name_5g, NULL);
                printk(KERN_ALERT "Error: Could not initialize /proc/%s\n", name_5g);
                return -ENOMEM;
        }
        procfile_5g->read_proc = procfile_read_5g;
        procfile_5g->write_proc = procfile_write_5g;
        procfile_5g->mode         = S_IFREG | S_IRUGO;
        procfile_5g->uid          = 0;
        procfile_5g->gid          = 0;
        procfile_5g->size         = 37;

        procfile_2g = create_proc_entry(name_2g, 0644, NULL);
        if (procfile_2g == NULL) {
                remove_proc_entry(name_2g, NULL);
                printk(KERN_ALERT "Error: Could not initialize /proc/%s\n", name_2g);
                return -ENOMEM;
        }
        procfile_2g->read_proc = procfile_read_2g;
        procfile_2g->write_proc = procfile_write_2g;
        procfile_2g->mode         = S_IFREG | S_IRUGO;
        procfile_2g->uid          = 0;
        procfile_2g->gid          = 0;
        procfile_2g->size         = 37;
	
        g_ioaddr_5g = ioremap(mmio_start_5g, mmio_len);
	g_ioaddr_2g = ioremap(mmio_start_2g, mmio_len);
	
	return 0;
}

void cleanup_module()
{
        remove_proc_entry(name_5g, NULL);
	remove_proc_entry(name_2g, NULL);
	iounmap(g_ioaddr_5g);
	iounmap(g_ioaddr_2g);
}

