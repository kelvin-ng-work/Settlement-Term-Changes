Function Global:Send-Email { 
[cmdletbinding()]
Param (
[Parameter(Mandatory=$False,Position=0)]
[String]$Address = "USER@DOMAIN",
[Parameter(Mandatory=$False,Position=1)]
[String]$Subject = "Test Email",
[Parameter(Mandatory=$False,Position=2)]
[String]$Body = "A test email."
      )
Begin {
Clear-Host
    }
Process {
$Outlook = New-Object -ComObject Outlook.Application
$Mail = $Outlook.CreateItem(0)
$Mail.To = "$Address"
$Mail.Subject = $Subject
$Mail.Body =$Body
# $Mail.HTMLBody = "HTML message body"
# $File = "C:\Users\kelvin.ng\Desktop\textfile.txt"
# $Mail.Attachments.Add($File)
$Mail.Send()
       }
End {
# $Outlook.Quit()
# [System.Runtime.Interopservices.Marshal]::ReleaseComObject($Outlook)
# $Outlook = $null
   }
}
Send-Email
