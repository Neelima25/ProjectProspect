package com.prospection.coding.assignment.service;

import com.prospection.coding.assignment.data.PurchaseRecordDAO;
import com.prospection.coding.assignment.domain.AnalysisResult;
import com.prospection.coding.assignment.domain.PurchaseRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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

        Map<Integer, List<PurchaseRecord>> patientMap = new HashMap<>();
        for (PurchaseRecord currentRecord : purchaseRecords) {
            int currentPId = currentRecord.getPatientId();
            if (patientMap.containsKey(currentPId)) {
                List<PurchaseRecord> list = patientMap.get(currentPId);
                list.add(currentRecord);
            } else {
                List<PurchaseRecord> list = new ArrayList<>();
                list.add(currentRecord);
                patientMap.put(currentPId, list);
            }
        }

//        Map<AnalysisResult.PatientType, List<Integer>> resultMap = new HashMap<>();
//        if (!resultMap.containsKey(category)) {
//            resultMap.put(category, new ArrayList<Integer>());
//        }
        Map<AnalysisResult.PatientType, List<Integer>> resultMap = new HashMap<>();

        for (Integer patientId : patientMap.keySet()) {
            List<PurchaseRecord> records = patientMap.get(patientId);
            AnalysisResult.PatientType category = categorize(records);

            if (!resultMap.containsKey(category)) {
                resultMap.put(category, new ArrayList<Integer>());
            }
            List<Integer> patientIds = resultMap.get(category);
            patientIds.add(patientId);

        }

        // then put real results in here
        AnalysisResult result = new AnalysisResult();
        result.putTotal(VIOLATED, resultMap.get(VIOLATED).size());
        result.putTotal(VALID_NO_COMED, resultMap.get(VALID_NO_COMED).size());
        result.putTotal(VALID_BI_SWITCH, resultMap.get(VALID_BI_SWITCH).size());
        result.putTotal(VALID_IB_SWITCH, resultMap.get(VALID_IB_SWITCH).size());
        result.putTotal(VALID_I_TRIAL, resultMap.get(VALID_I_TRIAL).size());
        result.putTotal(VALID_B_TRIAL, resultMap.get(VALID_B_TRIAL).size());
        return result;
    }

    public AnalysisResult.PatientType categorize(List<PurchaseRecord> purchaseRecords) {
        Collections.sort(purchaseRecords, new PRComparator());
        String prevMedicine = purchaseRecords.get(0).getMedication();
        int prevDay = purchaseRecords.get(0).getDay();
        int vioCount = 0;
        String tempMedicine = null;
        //step 3
        for (int i = 1; i < purchaseRecords.size(); i++) {
            PurchaseRecord rec = purchaseRecords.get(i);

            String currMedicine = rec.getMedication();
            if (currMedicine.equals(prevMedicine)) {
                continue;
            } else {

                if ((rec.getDay() - prevDay) < (prevMedicine.equals("B") ? 30 : 90)) {
                    vioCount++;
                    if (vioCount == 1 || vioCount == 2) {
                        tempMedicine = prevMedicine;
                    }
                }
            }
        }

        if (vioCount == 0) {
            return VALID_NO_COMED;
        }
        if (vioCount == 1) {
            return tempMedicine.equals("B") ? VALID_BI_SWITCH : VALID_IB_SWITCH;
        }
        if (vioCount == 2) {
            return tempMedicine.equals("B") ? VALID_B_TRIAL : VALID_I_TRIAL;
        } else {
            return VIOLATED;
        }
    }
}

class PRComparator implements Comparator<PurchaseRecord> {
    @Override
    public int compare(PurchaseRecord o1, PurchaseRecord o2) {
        return o1.getDay() - o2.getDay();
    }
}
