$ErrorActionPreference = 'Stop'

$projects = @('forge-1.20.1', 'neoforge-1.21.1')
$legacyNames = @(
    'SpellPoolScreen',
    'SpellPoolClientScreenBridge',
    'SaveSpellPoolPayload',
    'SyncSpellPoolPayload'
)

foreach ($project in $projects) {
    $sourceRoot = Join-Path $PSScriptRoot "..\\$project\\src\\main\\java"
    $source = (Get-ChildItem -Path $sourceRoot -Recurse -Filter '*.java' | ForEach-Object {
        Get-Content -Path $_.FullName -Raw
    }) -join [Environment]::NewLine

    foreach ($legacyName in $legacyNames) {
        if ($source -match [regex]::Escape($legacyName)) {
            throw "$project still contains legacy spell-pool list UI reference: $legacyName"
        }
    }

    if ($source -notmatch 'ChestMenu\.sixRows') {
        throw "$project random spell pool editor does not open a six-row chest"
    }

    if ($source -match 'returnUserItems') {
        throw "$project still returns spell pool items to the player"
    }

    if ($source -match 'level\.random\.nextIntBetweenInclusive') {
        throw "$project still randomizes spell levels for finale spellbooks"
    }

    if ($source -notmatch 'SpellPoolEntry') {
        throw "$project does not retain spell levels in its pool"
    }

    if ($source -match 'durationMultiplier|column_duration|applyDurationMultiplier') {
        throw "$project still contains the removed duration control"
    }
}

Write-Output 'Chest spell pool editor source check passed.'
