echo off
del /Q c:\winnt\system32\drivers\windrvr.sys 
del /Q c:\winnt\system32\drivers\wdusb.sys
del /Q c:\winnt\inf\dkar500x.inf 
del /Q c:\winnt\inf\dkar5210.inf 
del /Q c:\winnt\system32\drivers\dkkernel.sys
del /Q c:\winnt\system32\drivers\anwiwdm.sys

echo If you have not already done so, please uninstall any 
echo "Atheros ar5001 Diagnostic Kernel Driver" 
echo instances from device manager
pause
