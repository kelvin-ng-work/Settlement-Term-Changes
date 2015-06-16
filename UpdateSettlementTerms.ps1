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
# Sets up connection string
function global:Set-SqlConnection ( $server, $username, $password, $database ) {
	$SqlConnection.ConnectionString = "server=$server;user id=$username;password=$password;database=$database;pooling=false;Allow Zero Datetime=True;"
}

# Sets up sql adapter and sql command object
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
$localDBQuery = "UPDATE SECURITY SET SETTLEMENT_TERM=SETTLEMENT_TERM-1 WHERE MARKED='Y'"
$mysqlresults = Get-SqlDataTable $localDBQuery
# Runs SQL statement to retrieve updated securities
$localDBQuery = "SELECT * FROM SECURITY WHERE MARKED='Y'"
$mysqlresults = Get-SqlDataTable $localDBQuery
# Builds SQL statement to update OmegaDB3 database
ForEach ($result in $mysqlresults){
	$securitiesArray += $result
}
$omegaDBQuery = "UPDATE SECURITY SET SETTLEMENT_TERM=SETTLEMENT_TERM-1 WHERE"
$count=1
ForEach ($security in $securitiesArray) {
	if($count -ne $securitiesArray.length) {
		$omegaDBQuery += " SECURITY_ID=" + $security.SECURITY_ID + " OR"
	} else {
		$omegaDBQuery += " SECURITY_ID=" + $security.SECURITY_ID
	}
	$count += 1
}
# Connects to OmegaDB3 database
$server= "host"
$username= "user"
$password= "password"
$database= "database"
Set-SqlConnection $server $username $password $database
# Runs SQL statement to update OmegaDB3 database SECURITY table
$mysqlresults = Get-SqlDataTable $omegaDBQuery