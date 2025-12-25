package com.demo.upload.constants;

import java.util.*;

/**
 * 各部位分值与列名的映射关系
 * 参考Python代码中的create_body_part_score_mapping方法
 */
public class BodyPartScoreMapping {
    
    /**
     * 获取各部位分值与列名的映射
     * @return Map<部位名称, Map<分值, List<列名>>>
     */
    public static Map<String, Map<Integer, List<String>>> getMapping() {
        Map<String, Map<Integer, List<String>>> mapping = new HashMap<>();
        
        // 头颈部
        Map<Integer, List<String>> headNeckMapping = new HashMap<>();
        headNeckMapping.put(1, Arrays.asList(
            "头颈部损伤—①头部外伤后，头痛头晕",
            "②颈椎损伤，无骨折"
        ));
        headNeckMapping.put(2, Arrays.asList(
            "①意外事故致记忆丧失",
            "②嗜睡、木僵、迟钝，能被语言刺激唤醒",
            "③昏迷＜1h",
            "④单纯颅顶骨折",
            "⑤甲状腺挫伤",
            "⑥臂丛神经损伤",
            "⑦颈椎棘突或横突骨折或移位",
            "⑧颈椎轻度压缩骨折（≤20%）"
        ));
        headNeckMapping.put(3, Arrays.asList(
            "①昏迷1～6h",
            "②昏迷＜1h伴神经障碍",
            "③颅底骨折",
            "④粉碎、开放或凹陷性颅顶骨折、脑挫裂伤、蛛网膜下腔出血",
            "⑤颈动脉内膜撕裂、血栓形成",
            "⑥喉、咽挫伤",
            "⑦颈髓挫伤",
            "⑧颈椎或椎板、椎弓跟或关节突脱位或骨折",
            "⑨＞1个椎体的压缩骨折或前缘压缩＞20%"
        ));
        headNeckMapping.put(4, Arrays.asList(
            "①昏迷1～6h，伴神经障碍",
            "②昏迷6～24h",
            "③仅对疼痛刺激有恰当反应",
            "④颅骨骨折性凹陷＞2cm",
            "⑤脑膜破裂或组织缺失",
            "⑥颅内血肿≤100ml",
            "⑦颈髓不完全损伤",
            "⑧喉压轧伤",
            "⑨颈动脉内膜撕裂、血栓形成伴神经障碍"
        ));
        headNeckMapping.put(5, Arrays.asList(
            "①昏迷伴有不适当的动作",
            "②昏迷＞24h",
            "③脑干损伤",
            "④颅内血肿＞100ml",
            "⑤颈4或以下颈髓完全损伤"
        ));
        headNeckMapping.put(6, Arrays.asList(
            "①碾压骨折",
            "②脑干碾压撕裂",
            "③断头",
            "④颈3以上颈髓下轧、裂伤或完全断裂，有或无骨折"
        ));
        mapping.put("headNeck", headNeckMapping);
        
        // 面部
        Map<Integer, List<String>> faceMapping = new HashMap<>();
        faceMapping.put(1, Arrays.asList(
            "面部损伤—①角膜擦伤",
            "②舌浅表裂伤",  
            "③鼻骨或颌骨骨折（粉碎、移位或开放性骨折时加1分）",
            "④牙齿折断、撕裂或脱位"
        ));
        faceMapping.put(2, Arrays.asList(
            "①颧骨、眶骨、下颌体或下颌关节突骨折",
            "②LeFort Ⅰ型骨折",
            "③巩膜、角膜裂伤"
        ));
        faceMapping.put(3, Arrays.asList(
            "①视神经挫伤",
            "②LeFort Ⅱ型骨折"
        ));
        faceMapping.put(4, Arrays.asList(
            "LeFort Ⅲ型骨折"
        ));
        mapping.put("face", faceMapping);
        
        // 胸部
        Map<Integer, List<String>> chestMapping = new HashMap<>();
        chestMapping.put(1, Arrays.asList(
            "胸部损伤—①单个肋骨骨折",
            "②胸椎扭伤",
            "③胸壁挫伤",
            "④胸骨挫伤"
        ));
        chestMapping.put(2, Arrays.asList(
            "①2-3根肋骨骨折",
            "②胸骨骨折",
            "③胸椎脱位、棘突或横突骨折",
            "④胸椎轻度压缩骨折（≤20%）"
        ));
        chestMapping.put(3, Arrays.asList(
            "①单叶肺挫伤、裂伤",
            "②单侧血胸或气胸",
            "③膈肌破裂",
            "④肋骨骨折≥4根（有血胸、气胸或纵膈血肿时评分加1分）",
            "⑤锁骨下动脉或无名动脉内膜裂伤、血栓形成",
            "⑥轻度吸入性损伤",
            "⑦胸椎脱位，椎板、椎弓根或关节突骨折",
            "⑧椎体压缩骨折＞1个椎骨或高度＞20%"
        ));
        chestMapping.put(4, Arrays.asList(
            "①多叶肺挫伤、裂伤",
            "②纵膈血肿或气肿",
            "③双侧血气胸",
            "④连枷胸",
            "⑤心肌挫伤",
            "⑥张力性气胸",
            "⑦血胸≥1000ml",
            "⑧气管撕裂",
            "⑨主动脉内膜撕裂",
            "⑩锁骨下动脉或无名动脉重度裂伤",
            "11.脊髓不完全损伤综合征"
        ));
        chestMapping.put(5, Arrays.asList(
            "①重度主动脉裂伤",
            "②心脏裂伤",
            "③支气管、气管破裂",
            "④连枷胸、吸入烧伤需机械通气",
            "⑤喉、气管分离",
            "⑥多叶肺撕裂伤伴张力性气胸，纵膈积血、积气或血胸＞1000ml",
            "⑦脊髓裂伤或完全损伤"
        ));
        chestMapping.put(6, Arrays.asList(
            "①主动脉完全离断",
            "②胸部广泛碾压"
        ));
        mapping.put("chest", chestMapping);
        
        // 腹部
        Map<Integer, List<String>> abdomenMapping = new HashMap<>();
        abdomenMapping.put(1, Arrays.asList(
            "腹部损伤—①擦伤、挫伤，浅表裂伤：阴囊、阴道、阴唇、会阴",
            "②腰扭伤",
            "③血尿"
        ));
        abdomenMapping.put(2, Arrays.asList(
            "①挫伤，浅表裂伤：胃、肠系膜、小肠、膀胱、输尿管、尿道",
            "②轻度挫伤，裂伤：胃、肝、脾、胰",
            "③挫伤：十二指肠、结肠",
            "④腰椎脱位、横突或棘突骨折",
            "⑤腰椎轻度压缩性（≤20%）",
            "⑥神经根损伤"
        ));
        abdomenMapping.put(3, Arrays.asList(
            "①浅表裂伤：十二指肠、结肠、直肠",
            "②穿孔：小肠、肠系膜、膀胱、输尿管、尿道",
            "③大血管中度挫伤、轻度裂伤或血腹＞1000ml的肾、肝、脾、胰",
            "④轻度髂动、静脉裂伤后腹膜血肿",
            "⑤腰椎脱位或椎板、椎弓根、关节突骨折",
            "⑥椎体压缩骨折＞1个椎骨或＞20%前缘高度"
        ));
        abdomenMapping.put(4, Arrays.asList(
            "①穿孔：胃、十二指肠、结肠、直肠",
            "②穿孔伴组织缺失：胃、膀胱、小肠、输尿管、尿道",
            "③肝裂伤（浅表性）",
            "④严重髂动脉或静脉裂伤",
            "⑤不全截瘫",
            "⑥胎盘剥离"
        ));
        abdomenMapping.put(5, Arrays.asList(
            "①重度裂伤伴组织缺失或严重污染：十二指肠、结肠、直肠",
            "②复杂破裂：肝、脾、肾、胰",
            "③完全性腰髓损伤"
        ));
        abdomenMapping.put(6, Arrays.asList(
            "躯干横断"
        ));
        mapping.put("abdomen", abdomenMapping);
        
        // 四肢
        Map<Integer, List<String>> limbsMapping = new HashMap<>();
        limbsMapping.put(1, Arrays.asList(
            "四肢损伤—①挫伤：肘、肩、腕、踝",
            "②骨折、脱位：指、趾",
            "③扭伤：肩锁、肩、肘、指、腕、髋、踝、趾"
        ));
        limbsMapping.put(2, Arrays.asList(
            "①骨折：肱、桡、尺、腓、胫、锁骨、肩胛、腕、掌、跟、跗、跖骨、耻骨支或骨盆单纯骨折",
            "②脱位：肘、手、肩、肩锁关节",
            "③严重肌肉、肌腱裂伤",
            "④内膜裂伤、轻度撕裂：腕、肱、腘动脉，腕、股、腘静脉"
        ));
        limbsMapping.put(3, Arrays.asList(
            "①骨盆粉碎性骨折",
            "②股骨骨折",
            "③脱位：腕、踝、膝、髋",
            "④膝下和上肢断裂",
            "⑤膝韧带断裂",
            "⑥坐骨神经撕裂",
            "⑦内膜撕裂、轻度撕裂伤：股动脉",
            "⑧重度裂伤伴或不伴血栓形成：腋、腘动脉，腘、股静脉"
        ));
        limbsMapping.put(4, Arrays.asList(
            "①骨盆碾压性骨折",
            "②膝下外伤性离断、碾压伤",
            "③重度撕裂伤：股动脉或肱动脉"
        ));
        limbsMapping.put(5, Arrays.asList(
            "骨盆开放粉碎性骨折"
        ));
        mapping.put("limbs", limbsMapping);
        
        // 体表
        Map<Integer, List<String>> bodyMapping = new HashMap<>();
        bodyMapping.put(1, Arrays.asList(
            "体表损伤—①擦/挫伤：面/手≤25cm身体≤50cm",
            "②浅表裂伤：面/手≤5cm身体≤10cm",
            "③一度烧伤≤100%",
            "④二度～三度烧伤/脱套伤＜10%体表面积"
        ));
        bodyMapping.put(2, Arrays.asList(
            "①擦/挫伤：面/手＞25cm，身体＞50cm",
            "②裂伤：面/手＞5cm，身体＞10cm",
            "③二度或三度烧伤/脱套伤达10%～19%体表面积"
        ));
        bodyMapping.put(3, Arrays.asList(
            "二度或三度烧伤/脱套伤达20%～29%体表面积"
        ));
        bodyMapping.put(4, Arrays.asList(
            "二度或三度烧伤/脱套伤达30%～39%体表面积"
        ));
        bodyMapping.put(5, Arrays.asList(
            "二度或三度烧伤/脱套伤达40%～89%体表面积"
        ));
        bodyMapping.put(6, Arrays.asList(
            "二度或三度烧伤/脱套伤≥90%体表面积"
        ));
        mapping.put("body", bodyMapping);
        
        return mapping;
    }
    
    /**
     * 从列名中提取伤情描述
     * 移除部位名称前缀和开头的破折号
     */
    public static String extractDescription(String colName) {
        // 移除部位名称前缀
        String[] partNames = {"头颈部损伤", "面部损伤", "胸部损伤", "腹部损伤", "四肢损伤", "体表损伤"};
        for (String partName : partNames) {
            if (colName.contains(partName)) {
                colName = colName.replace(partName, "").trim();
                break;
            }
        }
        
        // 移除开头的破折号
        if (colName.startsWith("—")) {
            colName = colName.substring(1).trim();
        }
        
        return colName;
    }
}

