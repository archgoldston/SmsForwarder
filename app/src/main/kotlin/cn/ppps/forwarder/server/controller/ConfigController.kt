package cn.ppps.forwarder.server.controller

import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.App
import cn.ppps.forwarder.server.model.BaseRequest
import cn.ppps.forwarder.server.model.ConfigData
import cn.ppps.forwarder.server.model.DeviceMarkUpdateData
import cn.ppps.forwarder.utils.AppUtils
import cn.ppps.forwarder.utils.HttpServerUtils
import cn.ppps.forwarder.utils.PhoneUtils
import cn.ppps.forwarder.utils.SettingUtils
import com.yanzhenjie.andserver.annotation.*
import com.yanzhenjie.andserver.error.HttpException

@Suppress("PrivatePropertyName")
@RestController
@RequestMapping(path = ["/config"])
class ConfigController {

    private val TAG: String = CloneController::class.java.simpleName

    //远程查配置
    @CrossOrigin(methods = [RequestMethod.POST])
    @PostMapping("/query")
    fun test(@RequestBody bean: BaseRequest<*>): ConfigData {
        Log.d(TAG, bean.data.toString())

        //获取卡槽信息
        if (App.SimInfoList.isEmpty()) {
            App.SimInfoList = PhoneUtils.getSimMultiInfo()
        }
        Log.d(TAG, App.SimInfoList.toString())

        return ConfigData(
            HttpServerUtils.enableApiClone,
            HttpServerUtils.enableApiSmsSend,
            HttpServerUtils.enableApiSmsQuery,
            HttpServerUtils.enableApiCallQuery,
            HttpServerUtils.enableApiContactQuery,
            HttpServerUtils.enableApiContactAdd,
            HttpServerUtils.enableApiBatteryQuery,
            HttpServerUtils.enableApiWol,
            HttpServerUtils.enableApiLocation,
            HttpServerUtils.enableApiConfigQuery,
            HttpServerUtils.safetyMeasures,
            SettingUtils.extraDeviceMark,
            SettingUtils.extraSim1,
            SettingUtils.extraSim2,
            App.SimInfoList,
            AppUtils.getAppVersionCode(),
            AppUtils.getAppVersionName(),
        )
    }

    /**
     * 新增设备标识修改接口：沿用原应用的主动控制安全设置。
     * 不额外强制签名，不影响其他 HTTP 接口原有的校验逻辑。
     * safetyMeasures=0 时按原接口明文处理；=1 时由 AppMessageConverter 自动验签。
     */
    @CrossOrigin(methods = [RequestMethod.POST])
    @PostMapping("/device-mark/update")
    fun updateDeviceMark(@RequestBody bean: BaseRequest<DeviceMarkUpdateData>): String {
        val newMark = bean.data.extraDeviceMark?.trim() ?: ""
        if (newMark.isEmpty() || newMark.codePointCount(0, newMark.length) > 80 || newMark.toByteArray(Charsets.UTF_8).size > 240) {
            throw HttpException(500, "设备标识不能为空且不得超过80字/240字节")
        }
        SettingUtils.extraDeviceMark = newMark
        return if (SettingUtils.extraDeviceMark == newMark) "success" else "更新后回读设备标识不一致"
    }

}
