copy /Y .\DLL\AimControl.dll .\..\..\bin\AimControl.dll
copy /Y .\DLL\FMAnalysisDll.dll .\..\..\bin\FMAnalysisDll.dll
copy /Y .\DLL\iqapi.dll .\..\..\bin\iqapi.dll
copy /Y .\DLL\libiqapi.dll .\..\..\bin\libiqapi.dll

copy /Y .\DLL\IQ2010Ext.dll .\..\..\bin\IQ2010Ext.dll
copy /Y .\DLL\IQapiExt.dll .\..\..\bin\IQapiExt.dll
copy /Y .\DLL\IQlite_Logger.dll .\..\..\bin\IQlite_Logger.dll
copy /Y .\DLL\IQlite_Timer.dll .\..\..\bin\IQlite_Timer.dll
copy /Y .\DLL\IQmeasure.dll .\..\..\bin\IQmeasure.dll

copy /Y .\DLL\*.exe .\..\..\bin\.

copy /Y .\DLL\litepoint.dll .\..\..\bin\litepoint.dll
copy /Y .\litepoint_setup.txt .\..\..\bin\litepoint_setup.txt


IF NOT EXIST .\..\..\..\mod  MD .\..\..\..\mod
rem copy /Y .\mod\*.* .\..\..\..\mod\.


rem start /WAIT          .\..\vcredist_x86.exe



