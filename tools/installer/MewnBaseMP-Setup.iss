#ifndef SetupVersion
  #define SetupVersion "1.0.3"
#endif
#ifndef PayloadZip
  #define PayloadZip "MewnBaseMP-patch.zip"
#endif
#ifndef OutputDir
  #define OutputDir "Output"
#endif

[Setup]
AppId={{87E7C24F-8470-4F02-94B4-FADCE87FD676}
AppName=MewnBase Multiplayer Patch
AppVersion={#SetupVersion}
AppPublisher=MewnBase Multiplayer
DefaultDirName={code:GetDefaultGameDir}
UsePreviousAppDir=no
Uninstallable=no
PrivilegesRequired=admin
PrivilegesRequiredOverridesAllowed=commandline
ArchitecturesInstallIn64BitMode=x64compatible
DisableProgramGroupPage=yes
DisableReadyMemo=yes
WizardStyle=modern
OutputDir={#OutputDir}
OutputBaseFilename=MewnBaseMP-Setup-v{#SetupVersion}
Compression=lzma2/ultra64
SolidCompression=yes
SetupLogging=yes
RestartApplications=no
VersionInfoVersion={#SetupVersion}.0
VersionInfoProductName=MewnBase Multiplayer Patch
VersionInfoDescription=Installer for the MewnBase Multiplayer patch
VersionInfoCopyright=Community project

[Languages]
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl"

[Files]
Source: "{#PayloadZip}"; DestDir: "{tmp}"; DestName: "MewnBaseMP-patch.zip"; Flags: ignoreversion deleteafterinstall
Source: "Patch-MewnBaseJar.ps1"; DestDir: "{tmp}"; Flags: ignoreversion deleteafterinstall
Source: "InstallAction.dat"; DestDir: "{tmp}"; Flags: ignoreversion deleteafterinstall; AfterInstall: ApplyPatch

[Code]
var
  FoundGameDir: String;
  SteamLibraries: TArrayOfString;
  OperationPage: TInputOptionWizardPage;

function GetVdfValueFromFile(const FileName, Key: String): String;
var
  Lines: TArrayOfString;
  I, P, Q: Integer;
  Tail: String;
begin
  Result := '';
  if not FileExists(FileName) then
    Exit;
  if not LoadStringsFromFile(FileName, Lines) then
    Exit;
  for I := 0 to GetArrayLength(Lines) - 1 do begin
    P := Pos('"' + Key + '"', Lines[I]);
    if P = 0 then
      Continue;
    Tail := Trim(Copy(Lines[I], P + Length(Key) + 1, MaxInt));
    if (Length(Tail) < 2) or (Tail[1] <> '"') then
      Continue;
    Tail := Copy(Tail, 2, MaxInt);
    Q := Pos('"', Tail);
    if Q = 0 then
      Continue;
    Result := Copy(Tail, 1, Q - 1);
    StringChange(Result, '\\', '\');
    Exit;
  end;
end;

function IsGameRoot(const Root: String): Boolean;
begin
  Result := (Root <> '') and
    FileExists(AddBackslash(Root) + 'MewnBase.exe') and
    DirExists(AddBackslash(Root) + 'data') and
    FileExists(AddBackslash(Root) + 'game\desktop-1.0.jar');
end;

procedure ConsiderCandidate(const Candidate: String);
begin
  if (FoundGameDir = '') and IsGameRoot(Candidate) then
    FoundGameDir := RemoveBackslashUnlessRoot(Candidate);
end;

procedure AddSteamLibrary(const LibraryPath: String);
var
  I: Integer;
begin
  if LibraryPath = '' then
    Exit;
  for I := 0 to GetArrayLength(SteamLibraries) - 1 do
    if CompareText(RemoveBackslashUnlessRoot(SteamLibraries[I]), RemoveBackslashUnlessRoot(LibraryPath)) = 0 then
      Exit;
  SetArrayLength(SteamLibraries, GetArrayLength(SteamLibraries) + 1);
  SteamLibraries[GetArrayLength(SteamLibraries) - 1] := RemoveBackslashUnlessRoot(LibraryPath);
end;

procedure ScanSteamRoot(const SteamRoot: String);
var
  Lines: TArrayOfString;
  I: Integer;
  LibraryPath, InstallDir, ManifestPath: String;
begin
  if SteamRoot = '' then
    Exit;
  AddSteamLibrary(SteamRoot);
  if LoadStringsFromFile(AddBackslash(SteamRoot) + 'steamapps\libraryfolders.vdf', Lines) then begin
    for I := 0 to GetArrayLength(Lines) - 1 do begin
      if Pos('"path"', Lines[I]) > 0 then begin
        LibraryPath := Lines[I];
        LibraryPath := Copy(LibraryPath, Pos('"path"', LibraryPath) + 6, MaxInt);
        LibraryPath := Trim(LibraryPath);
        if (Length(LibraryPath) > 0) and (LibraryPath[1] = '"') then begin
          LibraryPath := Copy(LibraryPath, 2, MaxInt);
          if Pos('"', LibraryPath) > 0 then
            LibraryPath := Copy(LibraryPath, 1, Pos('"', LibraryPath) - 1);
          StringChange(LibraryPath, '\\', '\');
          AddSteamLibrary(LibraryPath);
        end;
      end;
    end;
  end;

  for I := 0 to GetArrayLength(SteamLibraries) - 1 do begin
    ManifestPath := AddBackslash(SteamLibraries[I]) + 'steamapps\appmanifest_743130.acf';
    InstallDir := GetVdfValueFromFile(ManifestPath, 'installdir');
    if InstallDir <> '' then
      ConsiderCandidate(AddBackslash(SteamLibraries[I]) + 'steamapps\common\' + InstallDir);
    ConsiderCandidate(AddBackslash(SteamLibraries[I]) + 'steamapps\common\MewnBase');
  end;
end;

function FindGameInstall: String;
var
  SteamRoot: String;
begin
  FoundGameDir := '';
  SetArrayLength(SteamLibraries, 0);

  if RegQueryStringValue(HKCU, 'Software\Valve\Steam', 'SteamPath', SteamRoot) then ScanSteamRoot(SteamRoot);
  if RegQueryStringValue(HKCU, 'Software\Valve\Steam', 'InstallPath', SteamRoot) then ScanSteamRoot(SteamRoot);
  if RegQueryStringValue(HKLM, 'Software\Valve\Steam', 'InstallPath', SteamRoot) then ScanSteamRoot(SteamRoot);
  if RegQueryStringValue(HKLM, 'Software\Wow6432Node\Valve\Steam', 'InstallPath', SteamRoot) then ScanSteamRoot(SteamRoot);
  ScanSteamRoot(ExpandConstant('{autopf}') + '\Steam');
  ScanSteamRoot(ExpandConstant('{autopf32}') + '\Steam');

  ConsiderCandidate(ExpandConstant('{userappdata}') + '\itch\apps\mewnbase');
  ConsiderCandidate(ExpandConstant('{localappdata}') + '\itch\apps\mewnbase');
  ConsiderCandidate(GetEnv('USERPROFILE') + '\Games\MewnBase');
  ConsiderCandidate('C:\Games\MewnBase');
  ConsiderCandidate('D:\Games\MewnBase');

  Result := FoundGameDir;
end;

function GetDefaultGameDir(Param: String): String;
begin
  if FoundGameDir = '' then
    FoundGameDir := FindGameInstall;
  if FoundGameDir <> '' then
    Result := FoundGameDir
  else
    Result := 'C:\Games\MewnBase';
end;

function GetBackupPath(const Root: String): String;
begin
  Result := AddBackslash(Root) + 'game\desktop-1.0_original.jar';
end;

procedure InitializeWizard;
begin
  FoundGameDir := FindGameInstall;
  if FoundGameDir <> '' then
    WizardForm.DirEdit.Text := FoundGameDir;

  if FoundGameDir = '' then
    WizardForm.SelectDirLabel.Caption :=
      'Совместимая папка MewnBase не найдена. Выберите папку игры — в ней должны быть MewnBase.exe, data и game\desktop-1.0.jar.'
  else
    WizardForm.SelectDirLabel.Caption :=
      'Найдена MewnBase. Проверьте путь или выберите другую установку игры.';

  OperationPage := CreateInputOptionPage(wpSelectDir,
    'Действие', 'Выберите, что сделать с выбранной установкой MewnBase',
    'При установке исходный JAR сохраняется рядом как desktop-1.0_original.jar.', True, False);
  OperationPage.Add('Установить или обновить мультиплеерный патч');
  OperationPage.Add('Восстановить исходный JAR из desktop-1.0_original.jar');
  OperationPage.SelectedValueIndex := 0;
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;
  if CurPageID = wpSelectDir then begin
    if not IsGameRoot(WizardDirValue) then begin
      MsgBox('В выбранной папке не найдена совместимая MewnBase. Нужны MewnBase.exe, папка data и game\desktop-1.0.jar. Выберите корневую папку игры.', mbError, MB_OK);
      Result := False;
    end;
  end else if (CurPageID = OperationPage.ID) and (OperationPage.SelectedValueIndex = 1) then begin
    if not FileExists(GetBackupPath(WizardDirValue)) then begin
      MsgBox('Резервная копия desktop-1.0_original.jar не найдена. Восстановление недоступно.', mbError, MB_OK);
      Result := False;
    end;
  end;
end;

procedure ApplyPatch;
var
  ResultCode: Integer;
  PowerShellPath, ActionName, Parameters, ErrorMessage: String;
begin
  ErrorMessage := '';
  try
    if OperationPage.SelectedValueIndex = 1 then
      ActionName := 'Restore'
    else
      ActionName := 'Install';
    PowerShellPath := ExpandConstant('{sys}\WindowsPowerShell\v1.0\powershell.exe');
    Parameters := '-NoLogo -NoProfile -NonInteractive -ExecutionPolicy Bypass -File "' +
      ExpandConstant('{tmp}\Patch-MewnBaseJar.ps1') + '" -Action ' + ActionName +
      ' -GameDir "' + WizardDirValue + '" -PayloadZip "' +
      ExpandConstant('{tmp}\MewnBaseMP-patch.zip') + '"';
    if not Exec(PowerShellPath, Parameters, '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then begin
      ErrorMessage := 'Не удалось запустить системный Windows PowerShell. Установка не выполнена.';
    end else if ResultCode <> 0 then
      ErrorMessage := 'Патч не применён. Исходный JAR не был заменён, резервная копия сохранена. Проверьте совместимость игры и закройте MewnBase. Журнал: ' + ExpandConstant('{%TEMP}') + '\MewnBaseMP-Setup.log';
  except
    ErrorMessage := 'Не удалось установить патч. Исходный JAR не был заменён. ' + GetExceptionMessage;
  end;
  if ErrorMessage <> '' then begin
    MsgBox(ErrorMessage, mbError, MB_OK);
    Abort;
  end;
end;
