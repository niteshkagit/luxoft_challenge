1. starter project was point to some old version[2.6.6](Spring Boot 2.6.6 is not supported. Please select a valid version.)
 which is not supported anymore,so I changed it to 2.7.17
( This can be further changed to use the latest spring boot version 3+)


==============================================================================

1. Created AccountBalanceServiceImpl : which transfers balance from source account to destination account
( thread safe manner
* Using a lock to make transfer between accounts synchronous
    a) if not valid transaction throw custom exception
    b) sends notification
2. Created custom exception InvalidTransactionException
3. Created related test classes

=======================Future possible enhancement for api==================
. Account status to be checked to both source and destination accounts
. May be Account class itself to be enhanced to support concurrency
. enhanced exception message
. API to be enhanced to be used in secured way (for both authentication and authorization)






