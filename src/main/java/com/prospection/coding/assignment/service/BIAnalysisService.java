package com.prospection.coding.assignment.service;

import com.prospection.coding.assignment.data.PurchaseRecordDAO;
import com.prospection.coding.assignment.domain.AnalysisResult;
import com.prospection.coding.assignment.domain.PurchaseRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.prospection.coding.assignment.domain.AnalysisResult.PatientType.*;

@Component
public class BIAnalysisService {

    private final PurchaseRecordDAO purchaseRecordDAO;

    @Autowired
    public BIAnalysisService(PurchaseRecordDAO purchaseRecordDAO) {
        this.purchaseRecordDAO = purchaseRecordDAO;
    }

    public AnalysisResult performBIAnalysis() throws Exception {
        List<PurchaseRecord> purchaseRecords = purchaseRecordDAO.allPurchaseRecords();
        // do some processing here

        Map<Integer,List<PurchaseRecord>> patientMap = new HashMap<>();
        for(PurchaseRecord currentRecord: purchaseRecords){
            int currentPId=currentRecord.getPatientId();
            if(patientMap.containsKey(currentPId)){
                List<PurchaseRecord> list=patientMap.get(currentPId);
                list.add(currentRecord);
            }
            else{
                List<PurchaseRecord> list= new ArrayList<>();
                list.add(currentRecord);
                patientMap.put(currentPId,list);
            }
        }

        // then put real results in here
        AnalysisResult result = new AnalysisResult();
        result.putTotal(VIOLATED, 200);
        result.putTotal(VALID_NO_COMED, 500);
        result.putTotal(VALID_BI_SWITCH, 100);
        result.putTotal(VALID_IB_SWITCH, 100);
        result.putTotal(VALID_I_TRIAL, 50);
        result.putTotal(VALID_B_TRIAL, 50);
        return result;
    }
}
