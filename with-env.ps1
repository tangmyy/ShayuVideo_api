# Load literal KEY=VALUE entries, then execute a command from the backend root.
# Dot-source this file without arguments to load variables into the current shell.
$envFile = Join-Path $PSScriptRoot '.env'
if (-not (Test-Path -LiteralPath $envFile)) {
    throw 'Missing .env. Copy .env.example to .env and fill in the configuration.'
}
$previousValues = @{}
foreach ($line in [System.IO.File]::ReadAllLines($envFile)) {
    if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith('#')) { continue }
    if ($line -notmatch '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        throw 'Invalid .env entry. Use KEY=VALUE without quotes or multiline values.'
    }
    $key = $Matches[1]
    $value = $Matches[2]
    if (-not $previousValues.ContainsKey($key)) {
        $previousValues[$key] = [Environment]::GetEnvironmentVariable($key, 'Process')
    }
    [Environment]::SetEnvironmentVariable($key, $value, 'Process')
}
if ($args.Count -gt 0) {
    $command = $args[0]
    $commandArgs = @($args | Select-Object -Skip 1)
    Push-Location $PSScriptRoot
    try {
        & $command @commandArgs
        if (-not $?) { throw 'Command failed. See the command output for details.' }
    } finally {
        Pop-Location
        foreach ($key in $previousValues.Keys) {
            [Environment]::SetEnvironmentVariable($key, $previousValues[$key], 'Process')
        }
    }
}
