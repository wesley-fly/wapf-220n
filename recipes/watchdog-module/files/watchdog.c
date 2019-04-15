#include <linux/module.h>
#include <linux/init.h>
#include <linux/sched.h>
#include <linux/timer.h>

#include <linux/kernel.h>
#include <linux/gpio.h>
//#include <asm-generic/gpio.h>

MODULE_DESCRIPTION("External Reset IC driver");
MODULE_AUTHOR("xa.Browan");
MODULE_LICENSE("GPL");

struct timer_list stimer; 

static void time_handler(unsigned long data){ 
	gpio_direction_output(224,1);
	gpio_direction_output(224,0);
	mod_timer(&stimer, jiffies + 20*HZ);
	//printk("current jiffies is %ld\n", jiffies);
}
static int __init timer_init(void){
	int status; 
	
	status = gpio_request(225,"WDI_ENABLE");
	if (status < 0) {
		printk("Request error!\n");
		return 1;
	}
	status = gpio_request(224,"FEED_DOG");
	if (status < 0) {
                printk("Request error!\n");
		return 1;
	}
	//gpio_direction_output(225,0);
	printk("Watchdog module loaded!\n");
	init_timer(&stimer);
	stimer.data = 0;
	stimer.expires = jiffies + HZ;
	stimer.function = time_handler;
	add_timer(&stimer);
	gpio_direction_output(225,0);

	return 0;
}
static void __exit timer_exit(void){
	printk("Unload watchdog module.\n");
        gpio_free(224);
        gpio_free(225);
	del_timer(&stimer);
}
module_init(timer_init);
module_exit(timer_exit);

