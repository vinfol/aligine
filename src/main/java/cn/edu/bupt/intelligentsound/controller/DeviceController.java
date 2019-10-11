package cn.edu.bupt.intelligentsound.controller;

import cn.edu.bupt.intelligentsound.utils.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@RequestMapping(value = "/control")
@Controller
public class DeviceController {

    private static final List<String> list = Arrays.asList("switch","curtain","dimmableLight");
    private static final Map<String, String> imageMap;
    static
    {
        imageMap = new HashMap<String, String>();
        imageMap.put("switch", "https://smart.gantch.cn/api/v1/wechatPost/download?imageName=5d7eb0bd-fd50-4998-ae76-7115d0c3a653.png");
        imageMap.put("curtain", "https://smart.gantch.cn/api/v1/wechatPost/download?imageName=3eb60dfe-1408-4cc4-b8d5-9e73a8bda2ee.png");
        imageMap.put("dimmableLight", "https://smart.gantch.cn/api/v1/wechatPost/download?imageName=edcce04a-c453-4673-aa1f-5825d7bd1191.png");
        imageMap.put("IASZone", "https://ss0.bdstatic.com/94oJfD_bAAcT8t7mm9GUKT-xh_/timg?image&quality=100&size=b4000_4000&sec=1531878000&di=c989660f4b827a0049c3b7aec4fe38e1&src=http://img.czvv.com/sell/599adfe4d2f0b1b2f118606f/20170905113247194.jpg");
    }

    //接收天猫精灵控制指令
    @RequestMapping(value = "/getOrder", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    private JSONObject getOrder(@RequestBody JSONObject message, HttpServletResponse response) throws IOException {
        System.out.println("getOrder");
        System.out.println(new Date());
        String namespace = message.getJSONObject("header").get("namespace").toString();
        System.out.println(namespace);
        if (namespace.equals("AliGenie.Iot.Device.Discovery")) {
            JSONObject answerJson = this.getAllDevices(message);
            System.out.println(answerJson);
            return answerJson;
        } else if (namespace.equals("AliGenie.Iot.Device.Control")) {
            JSONObject answerJson = this.controlDevice(message);
            System.out.println(answerJson);
            return answerJson;
        } else if (namespace.equals("AliGenie.Iot.Device.Query")) {
            JSONObject answerJson = this.queryDeviceData(message);
            System.out.println(answerJson);
            return answerJson;
        }
        return new JSONObject();
    }

    //查询所有设备
    @RequestMapping(value = "/getAllDevices")
    @ResponseBody
    private JSONObject getAllDevices(@RequestBody JSONObject message) {
        System.out.println(message);
        String accessToken = message.getJSONObject("payload").get("accessToken").toString();
        String url = "http://47.104.8.164/api/v1/deviceaccess/customerdevices/2/" + accessToken + "?limit=1000";
        try {
            String res = HttpUtil.getAllDevices_Service_DeviceAttr(url);
            JSONObject responseJson = JSONObject.parseObject(res);

            JSONObject newObject = new JSONObject();
            JSONObject newPayload = new JSONObject();
            List<JSONObject> devices = new ArrayList();

            JSONArray jsonArray = responseJson.getJSONArray("data");
            if (jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {

                    List<JSON> properties = new ArrayList();
                    JSONObject device = new JSONObject();
                    JSONObject propertie = new JSONObject();
                    List actions = new ArrayList();
                    JSONObject extentions = new JSONObject();

                    JSONObject element = jsonArray.getJSONObject(i);
                    if (list.contains(element.get("deviceType"))) {

                        device.put("deviceId", element.get("id"));
                        if(element.get("nickname") != null) {
                            device.put("deviceName", element.get("nickname"));
                        }else {
                            device.put("deviceName", element.get("name"));
                        }
                        if(element.get("deviceType").equals("dimmableLight")){
                            device.put("deviceType", "light");
                        }else{
                            device.put("deviceType", element.get("deviceType"));
                        }
                        device.put("zone", element.get("location"));
                        device.put("brand", element.get("manufacture"));
                        device.put("model",element.get("model"));
                        device.put("icon", imageMap.get(element.get("deviceType")));

                        propertie.put("name", "powerstate");
                        propertie.put("value", "off");
                        properties.add(propertie);
                        device.put("properties", properties);

                        actions.add("TurnOn");
                        actions.add("TurnOff");
                        if(element.get("deviceType").equals("dimmableLight")){
                            actions.add("SetBrightness");
                            actions.add("AdjustUpBrightness");
                            actions.add("AdjustDownBrightness");
                        }
                        device.put("actions", actions);

                        extentions.put("extension1", "test");
                        extentions.put("extension2", "test");
                        device.put("extentions", extentions);

                        devices.add(device);
                    }
                }
                newPayload.put("devices", devices);
            }
            newObject.put("payload", newPayload);
            newObject.put("header",message.getJSONObject("header"));
            newObject.getJSONObject("header").put("name", "DiscoveryDevicesResponse");
            System.out.println(newObject);

            return newObject;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

//    控制设备
    @RequestMapping(value = "/controlDevice")
    @ResponseBody
    private JSONObject controlDevice(@RequestBody JSONObject message) {
        String name = message.getJSONObject("header").get("name").toString();
        String deviceId = message.getJSONObject("payload").get("deviceId").toString();
        String attribute = message.getJSONObject("payload").get("attribute").toString();
        String value = message.getJSONObject("payload").get("value").toString();
        String deviceUrl = "http://47.104.8.164/api/v1/deviceaccess/device/" + deviceId;
        String operator = "";
        int bright = 0;
        if(attribute.equals("powerstate")){
            operator = "control switch";
        }else{
            operator = "control dimmableLight";
        }
        if(name.equals("SetBrightness")){
            if(value.equals("max")){
                bright = 255;
            }else if(value.equals("min")){
                bright = 1;
            }else{
                int t = Integer.decode(value);
                if(t>=255){
                    bright = 255;
                }else if(t<=1){
                    bright = 255;
                }else{
                    bright = t;
                }
            }
        }else if(name.equals("AdjustUpBrightness")){
            bright = 200;
        }else if(name.equals("AdjustDownBrightness")){
            bright = 100;
        }
        try {
            String deviceInformation = HttpUtil.getAllDevices_Service_DeviceAttr(deviceUrl);
            JSONObject deviceInformationObject = JSONObject.parseObject(deviceInformation);
            System.out.println(deviceInformationObject);


            String serviceUrl = "http://47.104.8.164/api/v1/servicemanagement/ability/" + deviceInformationObject.get("manufacture") + "/" + deviceInformationObject.get("deviceType") + "/" + deviceInformationObject.get("model");
            String deviceService = HttpUtil.getAllDevices_Service_DeviceAttr(serviceUrl);
            JSONArray serviceArray = JSONArray.parseArray(deviceService);
            JSONObject serviceObject2 = new JSONObject();
            for(int i=0;i<serviceArray.size();i++){
                serviceObject2 = JSONObject.parseObject(serviceArray.getJSONObject(i).get("abilityDes").toString());
                if(serviceObject2.get("serviceName").equals(operator)){
                    break;
                }
            }

            System.out.println(serviceObject2);

            String deviceAttrUrl = "http://47.104.8.164/api/v1/deviceaccess/allattributes/"+deviceId;
            String deviceAttr = HttpUtil.getAllDevices_Service_DeviceAttr(deviceAttrUrl);
            JSONArray deviceAttrArray1 = JSONArray.parseArray(deviceAttr);
            System.out.println(deviceAttrArray1);

            JSONObject serviceBody = serviceObject2.getJSONObject("serviceBody");
            JSONArray params = serviceBody.getJSONArray("params");

            JSONObject body = new JSONObject();
            body.put("serviceName",serviceObject2.get("serviceName"));
            body.put( "methodName",serviceBody.get("methodName"));

            if(params.size()>1){
                for(int i=0;i<params.size();i++){
                    String key = params.getJSONObject(i).get("key").toString();
                    for(int j=0;j<deviceAttrArray1.size();j++){
                        JSONObject element = deviceAttrArray1.getJSONObject(j);
                        if(element.get("key").equals(key)){
                            body.put(key,element.get("value"));
                            break;
                        }
                    }

                }
            }
            if(value.equals("on")){
                if(deviceInformationObject.get("deviceType").equals("curtain")){
                    body.put("status",1);
                }else{
                    body.put("status",true);
                }
            }else if(value.equals("off")){
                if(deviceInformationObject.get("deviceType").equals("curtain")){
                    body.put("status",0);
                }else{
                    body.put("status",false);
                }
            }else{
                    body.put("bright",bright);
            }

            System.out.println(body);
            Random ran = new Random();
            int requestId = ran.nextInt(1000)+1;
            String controlUrl = "http://47.104.8.164/api/v1/deviceaccess/rpc/"+deviceId+"/"+requestId;
            System.out.println(controlUrl);
            String answer = HttpUtil.sendControl(controlUrl,body);
            System.out.println(answer);

            JSONObject newObject = new JSONObject();
            JSONObject pay = new JSONObject();
            newObject.put("header",message.getJSONObject("header"));
            newObject.getJSONObject("header").put("name", message.getJSONObject("header").get("name")+"Response");
            pay.put("deviceId",deviceId);
            newObject.put("payload",pay);
            System.out.println(newObject);

            return newObject;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    //查询设备数据
    @RequestMapping(value = "/queryDeviceData")
    @ResponseBody
    private JSONObject queryDeviceData(@RequestBody JSONObject message){
        return new JSONObject();
    }

}
