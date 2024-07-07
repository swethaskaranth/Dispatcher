package com.goflash.dispatch.api_services

/**
 * Created by Binay on 7/11/18.
 */
object SessionService {

    var token: String = ""//""eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhcHAiOiJGTEFTSF9XRUIiLCJhdWQiOiJtZXJjdXJ5IiwidWlkIjoiMGI4NGRhN2ItZDE2MC00N2E1LTliNWEtZWIyNDUwZTgzZTZlIiwiZG9tYWluTmFtZSI6IkJPTFQiLCJpc3MiOiJQaGFybUVhc3kuaW4iLCJuYW1lIjoiQXJwaXQiLCJjbGllbnQiOiJQSCIsInNjb3BlcyI6WyJzdXBlci1hZG1pbiJdLCJleHAiOjE1NjA3NjQ4MDYsInZlcnNpb24iOiIxLjAuMCIsInVzZXIiOiJhcnBpdC5tYWx2aXlhQHBoYXJtZWFzeS5pbiIsInRlbmFudCI6IjEifQ.gN-aVMzlSsQlXj8fOuyOa5eeOcNtQf39ajZW559Xs7aB_oLX7HPA60i4Cml24scfXHdBybr7OO-a-dTRCpwN_w"
    var userId: String = ""
    var name: String = ""
    var username: String = ""
    var tenant: String = ""
    var assignedAssetName: String = ""
    var assignedAssetId : Long = 0
    var invoiceGenerationFlag : Boolean = false
    var auditActive : Boolean = false
    var token2: String ="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhcHAiOiJuZWJ1bGEiLCJhdWQiOiJtZXJjdXJ5IiwidWlkIjoiYm9sdEBwaGFybWVhc3kuaW4iLCJpc3MiOiJQaGFybUVhc3kuaW4iLCJuYW1lIjoiYm9sdCIsInN0b3JlIjoiIiwic2NvcGVzIjpbIndoLWRlYnVnIl0sInVzZXIiOiJib2x0QHBoYXJtZWFzeS5pbiIsInRlbmFudCI6IiJ9.Ym9ay89Iqi0tnnzifo5-rEqEVtSnbGiUL9KxAoJE1arQyewF64C-6LIKIACpsrd-oR1q2tiAfqllJtryXepd4A"
    var roles  = hashSetOf<String>()
    var email: String? = ""
    var selfAssignment: Boolean = false
}