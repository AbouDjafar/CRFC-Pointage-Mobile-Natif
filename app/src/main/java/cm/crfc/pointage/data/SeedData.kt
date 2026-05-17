package cm.crfc.pointage.data

import cm.crfc.pointage.data.local.AbsenceReasonDao
import cm.crfc.pointage.data.local.AbsenceReasonEntity
import cm.crfc.pointage.data.local.EmployeeDao
import cm.crfc.pointage.data.local.EmployeeEntity
import cm.crfc.pointage.data.local.UserDao
import cm.crfc.pointage.data.local.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SeedInstaller(
    private val userDao: UserDao,
    private val employeeDao: EmployeeDao,
    private val absenceReasonDao: AbsenceReasonDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun installIfNeeded() {
        scope.launch {
            if (userDao.count() == 0) {
                userDao.insertAll(listOf(seedAdmin))
            }
            if (employeeDao.count() == 0) {
                employeeDao.insertAll(seedEmployees)
            }
            if (absenceReasonDao.count() == 0) {
                absenceReasonDao.insertAll(seedReasons)
            }
        }
    }
}

private const val now = "2024-01-01T00:00:00"

private val seedAdmin = UserEntity(
    id = "crfc-admin-001",
    firstName = "Administrateur",
    lastName = "CRFC",
    email = "admin@crfc.cm",
    jobTitle = "Administrateur Systeme",
    password = "shazam!2023",
    role = "ADMIN",
    isActive = true,
    createdAt = now
)

private val seedReasons = listOf(
    AbsenceReasonEntity("r1", "Absence injustifiee"),
    AbsenceReasonEntity("r2", "Maladie"),
    AbsenceReasonEntity("r3", "Conge"),
    AbsenceReasonEntity("r4", "Mission"),
    AbsenceReasonEntity("r5", "Permission"),
    AbsenceReasonEntity("r6", "Formation"),
    AbsenceReasonEntity("r7", "Deplacement"),
    AbsenceReasonEntity("r8", "Autre")
)

private val employeeDefs = listOf(
    "emp-001|Simon Francois|YONGA BAKALAG|false",
    "emp-002|Jean Marie|AWOUMOU ETOGA|false",
    "emp-003|Martin Christian|MANDIO BAMBOCK|false",
    "emp-004|Philippe Louis|BITJOKA|false",
    "emp-005|Patrick Michel Ange|NEKE MPONG|false",
    "emp-006|Debora|NGO TONYE|false",
    "emp-007|Olive|NAA MENFOUNG|false",
    "emp-008|Joseph|OMGBA AWA|false",
    "emp-009|Suzanne Ida|MBES|false",
    "emp-010|Yvonne|MAGNITOUO|false",
    "emp-011|Jacques Duclos|DINGONG A BOULL|false",
    "emp-012|Stephanie epouse BITJOKA|WOLIBWON A BETSEN|true",
    "emp-013|Andre Cedric|YASSI|false",
    "emp-014|David Yvan|NGUESSI DIFFO|false",
    "emp-015|Chantal|NGONDI|false",
    "emp-016|Albert|GWETH MBOCK|false",
    "emp-017|Boris|NGUEFACK ATEUFACK|false",
    "emp-018|Abel|KOBEWO|false",
    "emp-019|Valery|ONONO|false",
    "emp-020|Yves Mathieu|TONYE MVOGO|false",
    "emp-021|Michelle Laure|BEKONO|false",
    "emp-022|Majolie|KAPSOU|false",
    "emp-023|Leslie|DOKEM|false",
    "emp-024|Ismael|NDJIDDA|true",
    "emp-025|Yves Bertrand|MESSI AKINI|false",
    "emp-026|Veronique|DILU SUMBU|false",
    "emp-027|Mireille|PEHM MAMA|false",
    "emp-028|Karl|MBALLA ONGOLO|false",
    "emp-029|Regine epouse EBODE|NOUBISSIE|true",
    "emp-030|Clarisse|OLANG AMOUGUI|false",
    "emp-031|Regine|BALLA NNANGA|false",
    "emp-032|Jean Charles|ABEGA|false",
    "emp-033||IKENG BINDO|true",
    "emp-034|Francois|BAMBOCK|false",
    "emp-035|Simon|NKOK|false",
    "emp-036|David|ANDEGUE|false",
    "emp-037|Florent|EZEMZE|false",
    "emp-038|Yanick Durant|ESSIMI|false",
    "emp-039|Karl|TCHOUFO TAKAM|false",
    "emp-040||ABDOULRAMANE ABOU DJAFAR|true",
    "emp-041|Hypolite Myriam|FONKEU|false"
)

private val seedJobTitles = listOf(
    "Agent d'accueil",
    "Responsable d'exploitation",
    "Directeur adjoint",
    "Controleur",
    "Comptable",
    "Ressources humaines",
    "Technicien maintenance",
    "Assistant logistique"
)

private val seedDepartments = listOf(
    "Service Administratif",
    "Logistique",
    "Ressources Humaines",
    "Maintenance",
    "Comptabilite",
    "Service Technique"
)

private val seedEmployees = employeeDefs.map { raw ->
    val parts = raw.split("|")
    val firstName = parts[1]
    val lastName = parts[2]
    val index = employeeDefs.indexOf(raw)
    val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").trim()
    EmployeeEntity(
        id = parts[0],
        fullName = fullName,
        firstName = firstName,
        lastName = lastName,
        jobTitle = seedJobTitles[index % seedJobTitles.size],
        department = seedDepartments[index % seedDepartments.size],
        isActive = true,
        needsReview = parts[3].toBoolean(),
        importSource = "CSV",
        importedAt = "2024-01-01",
        createdAt = now
    )
}
