param(
  [Parameter(Mandatory=$true)][string]$Code,
  [int]$Port = 8791
)
$ErrorActionPreference = 'Stop'
$tcp = [Net.Sockets.TcpClient]::new()
try {
  $tcp.Connect('127.0.0.1', $Port)
  $tcp.ReceiveTimeout = 30000
  $encoding = [Text.UTF8Encoding]::new($false)
  $writer = [IO.StreamWriter]::new($tcp.GetStream(), $encoding)
  $reader = [IO.StreamReader]::new($tcp.GetStream(), $encoding)
  $writer.WriteLine($Code.Replace("`r", ' ').Replace("`n", ' '))
  $writer.Flush()
  $reader.ReadToEnd()
} finally {
  $tcp.Dispose()
}
