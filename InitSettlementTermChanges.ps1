# Local database connection properties
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
# Runs SQL statement to retrieve updated securities
$localDBQuery = "SELECT * FROM STAGE_SECURITY WHERE MARKED='Y' AND SETTLEMENT_TERM<>3"
$mysqlresults = Get-SqlDataTable $localDBQuery
# Builds SQL statement to update OmegaDB3 database
ForEach ($result in $mysqlresults){
	$securitiesArray += $result
}
# Connects to OmegaDB3 database
$server= $ConfigFile.Settings.DatabaseSettings.StageServer
$username= $ConfigFile.Settings.DatabaseSettings.Username
$password= $ConfigFile.Settings.DatabaseSettings.Password
$database= $ConfigFile.Settings.DatabaseSettings.StageDatabase
Set-SqlConnection $server $username $password $database
# Updates OmegaDB from the local database
ForEach ($security in $securitiesArray) {
	# Runs SQL statement to update OmegaDB3 database SECURITY table
	$omegaDBQuery = "UPDATE SECURITY SET SETTLEMENT_TERM=" + $security.SETTLEMENT_TERM + " WHERE SECURITY_ID=" + $security.SECURITY_ID
	echo $($omegaDBQuery)
	$mysqlresults = Get-SqlDataTable $omegaDBQuery
}
