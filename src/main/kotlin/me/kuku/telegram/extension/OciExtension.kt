package me.kuku.telegram.extension

import com.oracle.bmc.Region
import com.oracle.bmc.model.BmcException
import kotlinx.coroutines.delay
import me.kuku.telegram.entity.OciEntity
import me.kuku.telegram.entity.OciService
import me.kuku.telegram.logic.OciLogic
import me.kuku.telegram.utils.AbilitySubscriber
import me.kuku.telegram.utils.TelegramSubscribe
import me.kuku.telegram.utils.inlineKeyboardButton
import me.kuku.utils.JobManager
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

@Service
class OciExtension(
    private val ociService: OciService
) {

    fun AbilitySubscriber.oci() {

        sub("oci", "oracle cloud管理") {
            val add = inlineKeyboardButton("新增api密钥", "addOci")
            val query = inlineKeyboardButton("查询及操作", "queryOci")
            val delete = inlineKeyboardButton("删除", "deleteOci")
            val markup = InlineKeyboardMarkup(listOf(
                listOf(add),
                listOf(query),
                listOf(delete)
            ))
            sendMessage("""
                oci管理，如何创建api密钥
                租户id: 右上角头像->租户设置->租户信息->ocid
                用户id: 右上角头像->用户设置->用户信息->ocid
                其他信息：右上角头像->用户设置->api密钥->添加API密钥
            """.trimIndent(), markup)
        }

    }

    private val ociCache = mutableMapOf<Long, OciEntity>()

    private val selectCache = mutableMapOf<Long, OciCache>()

    fun TelegramSubscribe.oci() {
        callback("addOci") {
            editMessageText("请发送租户id")
            val tenantId = nextMessage().text
            editMessageText("请发送用户id")
            val userid = nextMessage().text
            editMessageText("请发送api密钥配置文件中的fingerprint")
            val fingerprint = nextMessage().text
            editMessageText("请发送创建api密钥下载的私钥信息，请复制全部内容并发送")
            val privateKey = nextMessage().text
            editMessageText("请发送您这个api信息要显示的名字，即备注")
            val remark = nextMessage().text
            val regions = Region.values()
            val regionList = mutableListOf<List<InlineKeyboardButton>>()
            for (i in regions.indices step 2) {
                val buttons = mutableListOf(
                    inlineKeyboardButton(regions[i].regionId, "ociRegion-${regions[i].regionId}")
                )
                if (i + 1 < regions.size) {
                    buttons.add(inlineKeyboardButton(regions[i + 1].regionId, "ociRegion-${regions[i + 1].regionId}"))
                }
                regionList.add(buttons)
            }
            ociCache[tgId] = OciEntity().also {
                it.tenantId = tenantId
                it.userid = userid
                it.fingerprint = fingerprint
                it.privateKey = privateKey
                it.tgId = tgId
                it.remark = remark
            }
            JobManager.delay(1000 * 60 * 3) {
                ociCache.remove(tgId)
            }
            editMessageText("请选择账号所属的区域", InlineKeyboardMarkup(regionList), top = true)
        }

        callbackStartsWith("ociRegion-") {
            val ociEntity = ociCache[tgId] ?: error("缓存已失效，请重新新增oci的api信息")
            val regionId = query.data.substring(10)
            ociEntity.region = regionId
            OciLogic.listCompartments(ociEntity)
            ociService.save(ociEntity)
            editMessageText("保存oci的api信息成功")
        }

        callback("deleteOci") {
            val delete1 = inlineKeyboardButton("删除api密钥", "deleteOciKey")
            val delete2 = inlineKeyboardButton("删除创建实例自动任务", "deleteOciCreateInstanceTask")
            editMessageText("请选择删除方式，删除api密钥也会删除该密钥下面所有的自动任务", InlineKeyboardMarkup(listOf(
                listOf(delete1),
                listOf(delete2)
            )))
        }

        callback("deleteOciKey") {
            val ociList = ociService.findByTgId(tgId)
            val buttonList = mutableListOf<List<InlineKeyboardButton>>()
            for (ociEntity in ociList) {
                buttonList.add(listOf(inlineKeyboardButton(ociEntity.remark, "ociDelete-${ociEntity.id}")))
            }
            editMessageText("请选择你需要删除的oci的key", InlineKeyboardMarkup(buttonList))
        }

        callbackStartsWith("ociDelete-") {
            val id = query.data.split("-")[1]
            ociService.deleteById(id)
            editMessageText("删除oci的api信息成功")
        }

        callback("queryOci") {
            val ociList = ociService.findByTgId(tgId)
            val buttonList = mutableListOf<List<InlineKeyboardButton>>()
            for (ociEntity in ociList) {
                buttonList.add(listOf(inlineKeyboardButton(ociEntity.remark, "ociQuery-${ociEntity.id}")))
            }
            editMessageText("请选择要操作的条目", InlineKeyboardMarkup(buttonList))
        }

        callbackStartsWith("ociQuery-") {
            val id = query.data.split("-")[1]
            val ociEntity = ociService.findById(id) ?: error("选歪了")
            selectCache[tgId] = OciCache(ociEntity)
            JobManager.delay(1000 * 60 * 3){
                selectCache.remove(tgId)
            }
            val createInstance = inlineKeyboardButton("创建实例", "ociCom")
            editMessageText("请选择操作方式", InlineKeyboardMarkup(listOf(
                listOf(createInstance)
            )))
        }

        callback("deleteOciCreateInstanceTask") {
            val entityList = ociService.findByTgId(tgId)
            val list = mutableListOf<List<InlineKeyboardButton>>()
            for (ociEntity in entityList) {
                for ((i, createInstanceCache) in ociEntity.createInstanceList.withIndex()) {
                    list.add(listOf(
                        inlineKeyboardButton("${ociEntity.remark}-${createInstanceCache.shape}-${createInstanceCache.cpu}-${createInstanceCache.memory}", "deleteOciCreateInstanceTask-${ociEntity.id}-${i}")))
                }
            }
            editMessageText("请选择删除的创建实例自动任务", InlineKeyboardMarkup(list))
        }
        callback("deleteOciCreateInstanceTask-") {
            val arr = query.data.split("-")
            val id = arr[1]
            val num = arr[2].toInt()
            val ociEntity = ociService.findById(id) ?: error("未找到id")
            ociEntity.createInstanceList.removeAt(num)
            ociService.save(ociEntity)
            editMessageText("删除定时任务成功")
        }

    }

    fun TelegramSubscribe.operate() {
        before { set(selectCache[tgId] ?: error("缓存不存在，请重新选择")) }
        callback("ociCom") {
            val shape1 = inlineKeyboardButton("VM.Standard.E2.1.Micro（amd）", "ociSelShape-1")
            val shape2 = inlineKeyboardButton("VM.Standard.A1.Flex（arm）", "ociSelShape-2")
            editMessageText("请选择创建的实例形状", InlineKeyboardMarkup(listOf(listOf(shape1), listOf(shape2))))
        }
        callbackStartsWith("ociSelShape-") {
            val i = query.data.substring(12).toInt()
            val shape = if (i == 1) "VM.Standard.E2.1.Micro" else if (i == 2) "VM.Standard.A1.Flex" else error("选歪了")
            firstArg<OciCache>().createInstanceCache.shape = shape
            val os1 = inlineKeyboardButton("Oracle-Linux-9", "ociSelOs-1")
            val os2 = inlineKeyboardButton("Oracle-Linux-8", "ociSelOs-2")
            val os3 = inlineKeyboardButton("Oracle-Linux-7", "ociSelOs-3")
            val os4 = inlineKeyboardButton("Oracle-Linux-6", "ociSelOs-4")
            val os5 = inlineKeyboardButton("Canonical-Ubuntu-22.04", "ociSelOs-5")
            val os6 = inlineKeyboardButton("Canonical-Ubuntu-20.04", "ociSelOs-6")
            val os7 = inlineKeyboardButton("Canonical-Ubuntu-18.04", "ociSelOs-7")
            val os8 = inlineKeyboardButton("CentOS-8", "ociSelOs-8")
            val os9 = inlineKeyboardButton("CentOS-7", "ociSelOs-9")
            val markup = InlineKeyboardMarkup(listOf(
                listOf(os1),
                listOf(os2),
                listOf(os3),
                listOf(os4),
                listOf(os5),
                listOf(os6),
                listOf(os7),
                listOf(os8),
                listOf(os9)
            ))
            editMessageText("请选择镜像", markup)
        }
        callbackStartsWith("ociSelOs-") {
            val ociCache = firstArg<OciCache>()
            val createInstanceCache = ociCache.createInstanceCache
            val operaSystem: String
            val version: String
            when (query.data.substring(9).toInt()) {
                1 -> ("Oracle Linux" to "9").apply { operaSystem = first; version = second }
                2 -> ("Oracle Linux" to "8").apply { operaSystem = first; version = second }
                3 -> ("Oracle Linux" to "7").apply { operaSystem = first; version = second }
                4 -> ("Oracle Linux" to "6").apply { operaSystem = first; version = second }
                5 -> ("Canonical Ubuntu" to "22.04").apply { operaSystem = first; version = second }
                6 -> ("Canonical Ubuntu" to "20.04").apply { operaSystem = first; version = second }
                7 -> ("Canonical Ubuntu" to "18.04").apply { operaSystem = first; version = second }
                8 -> ("CentOS" to "8").apply { operaSystem = first; version = second }
                9 -> ("CentOS" to "7").apply { operaSystem = first; version = second }
                else -> error("未匹配的数字")
            }
            val imageList = OciLogic.listImage(ociCache.entity, operaSystem, operatingSystemVersion = version)
            val imageId = if (createInstanceCache.shape.contains("Flex")) {
                imageList.find { it.displayName.contains("aarch64") }!!.id
            } else {
                imageList.find { !it.displayName.contains("aarch64") }!!.id
            }
            editMessageText("请发送创建实例的cpu，请注意，永久免费服务器amd只有1h1g，arm合计4h24g")
            val cpu = nextMessage().text.toFloatOrNull() ?: error("您发送的不为浮点数字")
            editMessageText("请发送创建实例的内存，请注意，永久免费服务器amd只有1h1g，arm合计4h24g")
            val memory = nextMessage().text.toFloatOrNull() ?: error("您发送的不为浮点数字")
            editMessageText("请发送创建实例的磁盘，请数字，永久免费配额只有200G的磁盘，所以磁盘因在50G和200G之间")
            val volumeSize = nextMessage().text.toLongOrNull() ?: error("您发送的不为数字")
            editMessageText("请发送创建实例的root密码")
            val password = nextMessage().text
            val instance = try {
                OciLogic.launchInstance(
                    ociCache.entity,
                    imageId,
                    cpu,
                    memory,
                    volumeSize,
                    createInstanceCache.shape,
                    password
                )
            } catch (e: BmcException) {
                val exMessage = e.message!!
                if (exMessage.contains("Out of host capacity")) {
                    createInstanceCache.imageId = imageId
                    createInstanceCache.cpu = cpu
                    createInstanceCache.memory = memory
                    createInstanceCache.volumeSize = volumeSize
                    createInstanceCache.rootPassword = password
                    val button1 = inlineKeyboardButton("添加", "ociAddTask-1")
                    val button2 = inlineKeyboardButton("取消", "ociAddTask-0")
                    editMessageText("没有货了，创建实例失败，你可以把该任务添加到自动任务中，以自动创建，是否添加", InlineKeyboardMarkup(listOf(
                        listOf(button1),
                        listOf(button2)
                    )))
                } else {
                    editMessageText("创建实例失败，异常信息：${e.message}")
                }
                return@callbackStartsWith
            }
            editMessageText("创建实例成功，查询ip中")
            delay(1000 * 15)
            val attachment = OciLogic.oneVnicAttachmentsByInstanceId(ociCache.entity, instance.id)
            val vnic = OciLogic.getVnic(ociCache.entity, attachment.vnicId)
            val publicIp = vnic.publicIp
            editMessageText("""
                创建实例成功
                ip：$publicIp
            """.trimIndent())
        }
        callbackStartsWith("ociAddTask-") {
            val i = query.data.substring(11).toIntOrNull() ?: error("错误的代码")
            if (i == 1) {
                val ociCache = firstArg<OciCache>()
                val createInstanceCache = ociCache.createInstanceCache
                val entity = ociCache.entity
                entity.createInstanceList.add(createInstanceCache)
                ociService.save(entity)
                editMessageText("添加任务成功")
            } else {
                editMessageText("已取消")
            }
        }
    }

}

data class OciCache(
    val entity: OciEntity,
    val createInstanceCache: CreateInstanceCache = CreateInstanceCache()
)

class CreateInstanceCache {
    var shape: String = ""
    var imageId: String = ""
    var cpu: Float = 0f
    var memory: Float = 0f
    var volumeSize: Long = 0
    var rootPassword: String = ""
}