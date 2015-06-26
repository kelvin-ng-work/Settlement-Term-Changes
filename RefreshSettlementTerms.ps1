# Database connection properties
$server= "host"
$username= "user"
$password= "password"
$database= "database"
$localDBQuery= ""
$omegaDBQuery= ""
$securitiesArray = @()
# The path to MySQL Connector
[void][system.reflection.Assembly]::LoadFrom("C:\...\MySQL.Data.dll")
Set-Variable SqlConnection (New-Object MySql.Data.MySqlClient.MySqlConnection) -Scope Global -Option AllScope
# Import database server settings from config file
[xml]$ConfigFile = Get-Content "C:\...\OmegaDBConnSettings.xml"

# Sets up connection string
function global:Set-SqlConnection ( $server, $username, $password, $database ) {
	$SqlConnection.ConnectionString = "server=$server;user id=$username;password=$password;database=$database;pooling=false;Allow Zero Datetime=True;"
}

# Sets up SQL adapter and SQL command object
function global:Get-SqlDataTable( $localDBQuery ) {
	if (-not ($SqlConnection.State -like "Open")) { $SqlConnection.Open() }
	$SqlCmd = New-Object MySql.Data.MySqlClient.MySqlCommand $localDBQuery, $SqlConnection
	$SqlAdapter = New-Object MySql.Data.MySqlClient.MySqlDataAdapter
	$SqlAdapter.SelectCommand = $SqlCmd
	$DataSet = New-Object System.Data.DataSet
	$SqlAdapter.Fill($DataSet) | Out-Null
	$SqlConnection.Close()
	return $DataSet.Tables[0]
}

# Connects to local database
Set-SqlConnection $server $username $password $database
# Runs SQL statement to update local database
$localDBQuery = "UPDATE STAGE_SECURITY SET SETTLEMENT_TERM=3, MARKED='N', TRADE_DATE=NULL"
$mysqlresults = Get-SqlDataTable $localDBQuery
# Connects to OmegaDB3 database
$server= $ConfigFile.Settings.DatabaseSettings.StageServer
$username= $ConfigFile.Settings.DatabaseSettings.Username
$password= $ConfigFile.Settings.DatabaseSettings.Password
$database= $ConfigFile.Settings.DatabaseSettings.StageDatabase
Set-SqlConnection $server $username $password $database
# Runs SQL statement to update OmegaDB3 database SECURITY table
$omegaDBQuery = "UPDATE SECURITY SET SETTLEMENT_TERM=3 WHERE PERMANENTLY_DELISTED='N'"
$mysqlresults = Get-SqlDataTable $omegaDBQuery