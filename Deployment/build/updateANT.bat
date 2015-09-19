@echo off
 > %temp%\~tmp.vbs echo sUrl = "http://mirror.csclub.uwaterloo.ca/apache//ant/binaries/apache-ant-1.9.6-bin.zip"
>> %temp%\~tmp.vbs echo sFolder = "%CD%/../"
>> %temp%\~tmp.vbs (findstr "'--VBS" "%0" | findstr /v "findstr")
cscript //nologo %temp%\~tmp.vbs
del /q %temp%\~tmp.vbs
move %temp%\apache-ant-1.9.6-bin.zip.extracted\apache-ant-1.9.6 ../apache-ant-1.9.6
del /q /s %temp%\apache-ant-1.9.6-bin.zip.extracted
pause
../../deploy.bat
goto :eof

'--- figure out temp file & folder
With CreateObject("WScript.Shell")  '--VBS
    sTempFile = .Environment("Process").Item("TEMP") & "\apache-ant-1.9.6-bin.zip"  '--VBS 
    sTempFolder = .Environment("Process").Item("TEMP") & "\apache-ant-1.9.6-bin.zip.extracted"  '--VBS
End With    '--VBS

'--- download
WiTh CreateObject("MSXML2.XMLHTTP") '--VBS
    .Open "GET", sUrl, false    '--VBS
    .Send() '--VBS
    If .Status = 200 Then   '--VBS
        ResponseBody = .ResponseBody    '--VBS
        With Createobject("Scripting.FileSystemObject") '--VBS
            If .FileExists(sTempFile) Then  '--VBS
                .DeleteFile sTempFile   '--VBS
            End If  '--VBS
        End With    '--VBS
        With CreateObject("ADODB.Stream")   '--VBS
            .Open   '--VBS
            .Type = 1 ' adTypeBinary    '--VBS
            .Write ResponseBody '--VBS
            .Position = 0   '--VBS
            .SaveToFile sTempFile   '--VBS
        End With    '--VBS
    End If  '--VBS
End With    '--VBS

'--- extract
With CreateObject("Scripting.FileSystemObject") '--VBS
    On Error Resume Next    '--VBS
    .CreateFolder sFolder   '--VBS
    .DeleteFolder sTempFolder, True '--VBS
    .CreateFolder sTempFolder   '--VBS
    On Error GoTo 0 '--VBS
    With CreateObject("Shell.Application")  '--VBS
        .NameSpace(sTempFolder).CopyHere .NameSpace(sTempFile).Items    '--VBS
    End With    '--VBS
    .DeleteFile sTempFile, True '--VBS
End With    '--VBS