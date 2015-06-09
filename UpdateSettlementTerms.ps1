# Database connection properties
$server= "localhost"
$username= "USER"
$password= "PASSWORD"
$database= "DATABASE"
$Query= ""
# The path to MySQL Connector
[void][system.reflection.Assembly]::LoadFrom("FOLDERPATH\MySQL.Data.dll")

# Sets up connection string
function global:Set-SqlConnection ( $server = $(Read-Host "SQL Server Name"), $username = $(Read-Host "Username"), $password = $(Read-Host "Password"), $database = $(Read-Host "Default Database") ) {
	$SqlConnection.ConnectionString = "server=$server;user id=$username;password=$password;database=$database;pooling=false;Allow Zero Datetime=True;"
}

# Sets up sql adapter and sql command object
function global:Get-SqlDataTable( $Query ) {
	if (-not ($SqlConnection.State -like "Open")) { $SqlConnection.Open() }
	$SqlCmd = New-Object MySql.Data.MySqlClient.MySqlCommand $Query, $SqlConnection
	$SqlAdapter = New-Object MySql.Data.MySqlClient.MySqlDataAdapter
	$SqlAdapter.SelectCommand = $SqlCmd
	$DataSet = New-Object System.Data.DataSet
	$SqlAdapter.Fill($DataSet) | Out-Null
	$SqlConnection.Close()
	return $DataSet.Tables[0]
}

# Connects to database and executes sql statement
Set-Variable SqlConnection (New-Object MySql.Data.MySqlClient.MySqlConnection) -Scope Global -Option AllScope -Description "Personal variable for Sql Query functions"
Set-SqlConnection $server $username $password $database
$Query = "UPDATE SECURITY_TABLE SET SETTLEMENT_TERM=SETTLEMENT_TERM-1 WHERE MARKED='Y'"
$mysqlresults = Get-SqlDataTable $Query
