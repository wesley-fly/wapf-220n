echo off
copy /Y anwiwdm.sys %SystemRoot%\system32\drivers
copy /Y anwi.inf %SystemRoot%\inf

echo If you have not already done so, please uninstall any 
echo "Atheros ar5001 Diagnostic Kernel Driver" 
echo instances from device manager
echo Now scan for new hardware in device manager, when complete ensure you have an
echo "Atheros AR5001 Anwi Diagnostic Kernel Driver" 
echo instance in device manager
echo If you have memory allocation problems in ART please reboot and try again

pause
