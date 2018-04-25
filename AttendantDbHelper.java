/*
 * Class : database.AttendantDbHelper
 * Author : iT Gurus Software
 * Copyright (C) 2017, iT Gurus Software. All rights reserved.
 */

package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import models.AllergicToModel;
import models.DBAppointmentModel;
import models.DBDiagnosisMedicineMappingModel;
import models.DBMedicineTypeModel;
import models.DBOPDClinic;
import models.DBPatientModel;
import models.DBPrescriptionMappingModel;
import models.DBPrescriptionModel;
import models.DBTxnOPDModel;
import models.DiagnosisModel;
import models.MedicalRecordImagesModel;
import models.MedicalRecordModel;
import models.MedicineListModel;
import models.PatientsListmodel;
import utils.Constants;

import static utils.Constants.COL_OPDDoctorUniqueId;

/**
 * Created by swati.hokale on 08-06-2016.
 */
public class AttendantDbHelper extends SQLiteOpenHelper
{
    private static AttendantDbHelper mInstance = null;
    private static final String KEY = "dp@#noj*ndfGN24rg";

    private Context mContext;
    private SQLiteDatabase db;

    public AttendantDbHelper(Context context)
    {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        mContext = context;
    }

    public static synchronized AttendantDbHelper getInstance(Context context)
    {
        if (mInstance == null)
        {
            mInstance = new AttendantDbHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        this.db = db;
        db.execSQL(Constants.CREATE_TABLE_TxnOPD);
        db.execSQL(Constants.CREATE_TABLE_APPOINTMENT);
        db.execSQL(Constants.CREATE_TABLE_OPDClinic);
        db.execSQL(Constants.CREATE_TABLE_NOTIFICATION);
        db.execSQL(Constants.CREATE_TABLE_PATIENT);
        db.execSQL(Constants.CREATE_TABLE_MEDICINE);
        db.execSQL(Constants.CREATE_TABLE_DIAGNOSIS);
        db.execSQL(Constants.CREATE_TABLE_ALLERGY);
        db.execSQL(Constants.CREATE_TABLE_MEDICAL_RECORD);
        db.execSQL(Constants.CREATE_TABLE_DIAGNOSISMEDICINE_MAPPING);
        db.execSQL(Constants.CREATE_TABLE_MEDICINETYPE);
        db.execSQL(Constants.CREATE_TABLE_PRESCRIPTION);
        db.execSQL(Constants.CREATE_TABLE_PRESCRIPTIONMEDICINE_MAPPING);
        db.execSQL(Constants.CREATE_TABLE_MEDICALRECORD_FILE);
        db.execSQL(Constants.CREATE_TABLE_TempPrescMedicineType);

        db.execSQL(Constants.CREATE_TABLE_ACCOUNTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        for (int i = oldVersion; i < newVersion; i++)
        {
            switch (i)
            {
 /*
                Old app implementation.. not required for new app
                case 1:
                    db.execSQL(Constants.CREATE_TABLE_ACCOUNTS);
                    break;
                case 2:
//                    1. alter appointment table
//                    2. alter transaction table, appointmentID
//                    3. alter medical record table, addedFrom coloum
//                    4. alter patientTable for field IsNewRecord
                    db.execSQL(addIntegerColoum(Constants.TABLE_APPOINTMENT, Constants.COL_IsFeeApplicable, 0));
                    db.execSQL(addIntegerColoum(Constants.TABLE_APPOINTMENT, Constants.COL_FeePaidAmount, 0));
                    db.execSQL(addIntegerColoum(Constants.TABLE_APPOINTMENT, Constants.COL_IsBalanceDue, 0));
                    db.execSQL(addIntegerColoum(Constants.TABLE_APPOINTMENT, Constants.COL_BalanceAmount, 0));

                    db.execSQL(addTextColoum(Constants.TABLE_Accounts, Constants.COL_AppointmentId));
                    db.execSQL(addIntegerColoum(Constants.TABLE_MedicalRecord, Constants.COL_AddedFrom, 1));
                    db.execSQL(addIntegerColoum(Constants.TABLE_Patient, Constants.COL_IsNewRecord, Constants.IsNewRecord_FALSE));
                break;*/

                case 1:
                    //1. alter patient table
                    db.execSQL(addTextColoum(Constants.TABLE_Patient, Constants.COL_LastConsultationDate));
                    break;
                default:
                    break;
            }
        }
    }

    private static void encrypt(Context ctxt, String dbName, String passphrase) throws IOException
    {
        File originalFile = ctxt.getDatabasePath(dbName);

        if (originalFile.exists())
        {
            File newFile = File.createTempFile("sqlcipherutils", "tmp", ctxt.getCacheDir());
            SQLiteDatabase db = SQLiteDatabase.openDatabase(originalFile.getAbsolutePath(), "", null,
                    SQLiteDatabase.OPEN_READWRITE);
//            SQLiteDatabase db = SQLiteDatabase.openDatabase(originalFile.getAbsolutePath(), passphrase, null,
//                    SQLiteDatabase.OPEN_READWRITE);

            db.rawExecSQL(String.format("ATTACH DATABASE '%s' AS encrypted KEY '%s';",
                    newFile.getAbsolutePath(), passphrase));
            db.rawExecSQL("SELECT sqlcipher_export('encrypted')");
            db.rawExecSQL("DETACH DATABASE encrypted;");
            int version = db.getVersion();
//            db.close();
            if (db != null)
            {
                db.close();
            }

            db = SQLiteDatabase.openDatabase(newFile.getAbsolutePath(), passphrase, null,
                    SQLiteDatabase.OPEN_READWRITE);
            db.setVersion(version);
//            db.close();
            if (db != null)
            {
                db.close();
            }

            originalFile.delete();
            newFile.renameTo(originalFile);
        }
    }

//    public static native String DatabaseJNICPP();

   /* static
    {
        System.loadLibrary("RetailerAppJNI");
    }*/

    public SQLiteDatabase getWriteable(Context context)
    {
        mContext = context;
        try
        {
            db = this.getWritableDatabase(KEY);
        } catch (Exception e)
        {
            try
            {
                encrypt(mContext, Constants.DATABASE_NAME, KEY);
                db = this.getWritableDatabase(KEY);
            } catch (IOException e1)
            {
                e1.printStackTrace();
                return null;
            }
        }
        return db;
    }

    public void close()
    {
        // NOTE: openHelper must now be a member of CallDataHelper;
        // you currently have it as a local in your constructor
        if (db != null)
        {
            db.close();
        }
    }

    public void clearDatabase(String TABLE_NAME)
    {
        String clearDBQuery = "DELETE FROM " + TABLE_NAME;
        db.execSQL(clearDBQuery);
    }

    //insert data in table TxnOPD
    public void save(DBTxnOPDModel model)
    {
        //db.beginTransaction(); 
        ContentValues values = new ContentValues();
        values.put(Constants.COL_TxnOPDUniqueId, model.getTxnOPDId());
        values.put(Constants.COL_TxnOPDClinicUniqueId, model.getOPDClinicId());
        values.put(Constants.COL_TxnClinicName, model.getClinicName());
        values.put(Constants.COL_TxnDate, model.getDate());
        values.put(Constants.COL_TxnFromTime, model.getFromTime());
        values.put(Constants.COL_TxnToTime, model.getToTime());
        values.put(Constants.COL_TxnCTOPDStatus, model.getCTOPDStatus());
        values.put(Constants.COL_TxnMaxAppointment, model.getMaxAppointment());
        values.put(Constants.COL_TxnLastGeneratedTokenValue, model.getLastGeneratedTokenValue());

       /* values.put(Constants.COL_TxnIsDoctorIn, model.getIsDoctorIn());
        values.put(Constants.COL_TxnActualStartTime, model.getActualStartTime());
        values.put(Constants.COL_TxnNextPatientTokenValue, model.getNextPatientTokenValue());
        values.put(Constants.COL_TxnCurrentPatientTokenValue, model.getCurrentPatientTokenValue());
        values.put(Constants.COL_TxnPendingPatientCount, model.getPendingPatientCount());
        values.put(Constants.COL_TxnCTDataSentToServerStatus, model.getCTDataSentToServerStatus());
        values.put(Constants.COL_TxnIsDeleted, model.getIsDeleted());
        values.put(Constants.COL_TxnCreatedBy, model.getCreatedBy());
        values.put(Constants.COL_TxnCreatedDtm, model.getCreatedDtm());
        values.put(Constants.COL_TxnUpdatedBy, model.getUpdatedBy());
        values.put(Constants.COL_TxnUpdatedDtm, model.getUpdatedDtm());
        values.put(Constants.COL_TxnExpectedStartTime, model.getExpectedStartTime());*/

        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_TxnOPD, Constants.COL_TxnOPDUniqueId, model.getTxnOPDId()))
        {
            //---UPDATE
            db.updateWithOnConflict(Constants.TABLE_TxnOPD, values,
                    Constants.COL_TxnOPDUniqueId + " = '" + model.getTxnOPDId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            //---ADD
            db.insert(Constants.TABLE_TxnOPD, null, values);
        }
        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    //insert data in table TABLE_APPOINTMENT
    public void save(DBAppointmentModel model)
    {
        //db.beginTransaction(); 
        ContentValues values = new ContentValues();
        values.put(Constants.COL_APPAge, model.getAge());
        values.put(Constants.COL_APPCTGender, model.getCTGender());
        values.put(Constants.COL_APPCTStatus, model.getCTStatus());//----1
        values.put(Constants.COL_APPMemberId, model.getMemberId());
        values.put(Constants.COL_APPTxnOPDUniqueId, model.getTxnOPDId());
        values.put(Constants.COL_APPUniqueId, model.getAppointmentId());
        values.put(Constants.COL_APPUpdatedBy, model.getUpdatedBy());
        values.put(Constants.COL_APPCreatedBy, model.getCreatedBy());
        values.put(Constants.COL_APPIsDeleted, model.getIsDeleted());
        values.put(Constants.COL_APPCreatedDtm, model.getCreatedDtm());
        values.put(Constants.COL_APPCTBookedBy, model.getCTBookedBy());
        values.put(Constants.COL_APPUpdatedDtm, model.getUpdatedDtm());
        values.put(Constants.COL_APPTokenValue, model.getTokenValue());//----2
        values.put(Constants.COL_APPAttendantId, model.getAttendantId());
        values.put(Constants.COL_APPPatientName, model.getPatientName());//----3
        values.put(Constants.COL_APPCTVisitType, model.getCTVisitType());
        values.put(Constants.COL_APPContactNumber, model.getContactNumber());//----4
        values.put(Constants.COL_APPSortOrder, model.getSortorder());//----5
        values.put(Constants.COL_APPAppointmentAddedDtmUTC, model.getAppointmentAddedDtmUTC());
        values.put(Constants.COL_APPIsNewRecord, model.getIsNewRecord());
        values.put(Constants.COL_APPCTDataSentToServerStatus, model.getCTDataSentToServerStatus());
        values.put(Constants.COL_APPRequestReceivedAtDtm, model.getRequestReceivedAtDtm());
        values.put(Constants.COL_PatientId, model.getPatientId());
        values.put(Constants.COL_IsFeeApplicable, model.getIsFeeApplicable());
        values.put(Constants.COL_FeePaidAmount, model.getFeeAmount());
        values.put(Constants.COL_IsBalanceDue, model.getIsDueAmount());
        values.put(Constants.COL_BalanceAmount, model.getDueAmount());
        db.insert(Constants.TABLE_APPOINTMENT, null, values);

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    public void save(DBOPDClinic model)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_OPDClinicUniqueId, model.getOPDClinicUniqueId());
        values.put(COL_OPDDoctorUniqueId, model.getDoctorUniqueId());
        values.put(Constants.COL_OPDName, model.getOPDName());
        values.put(Constants.COL_OPDLocality, model.getLocality());
        values.put(Constants.COL_OPDAvgConsultationTimeFirstVisitMins, model.getAvgConsultationTimeFirstVisitMins());
        values.put(Constants.COL_OPDAvgConsultationTimeFollowUpVisitMins, model.getAvgConsultationTimeFollowUpVisitMins());
        values.put(Constants.COL_OPDAdvanceBookingInDays, model.getAdvanceBookingInDays());
        values.put(Constants.COL_OPDAllowAttendantCancelOPD, model.getAllowAttendantCancelOPD());
        values.put(Constants.COL_OPDAllowAttendantModifyMaxPatient, model.getAllowAttendantModifyMaxPatient());
        values.put(Constants.COL_OPDIsDeleted, model.getIsDeleted());
        values.put(Constants.COL_OPDCreatedBy, model.getCreatedBy());
        values.put(Constants.COL_OPDCreatedDtm, model.getCreatedDtm());
        values.put(Constants.COL_OPDUpdatedBy, model.getUpdatedBy());
        values.put(Constants.COL_OPDUpdatedDtm, model.getUpdatedDtm());

        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_OPDClinic, Constants.COL_OPDClinicUniqueId, model.getOPDClinicUniqueId()))
        {
            db.updateWithOnConflict(Constants.TABLE_OPDClinic, values,
                    Constants.COL_OPDClinicUniqueId + " = '" + model.getOPDClinicUniqueId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            db.insert(Constants.TABLE_OPDClinic, null, values);
        }

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    public String getDoctorID()
    {
        String query = " SELECT " + COL_OPDDoctorUniqueId + " FROM  " + Constants.TABLE_OPDClinic;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        String doctorId = "";
        if (c.getCount() > 0)
        {
            while (!c.isAfterLast())
            {
                doctorId = c.getString(c.getColumnIndex(Constants.COL_OPDDoctorUniqueId));
                c.moveToNext();
            }
        }
        c.close();
        return doctorId;
    }

    /**
     * Save medicines against medicineID
     *
     * @param lAddMedicineListModel
     */
    public void save(MedicineListModel lAddMedicineListModel)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_MedicineId, lAddMedicineListModel.getMedicineID());
        values.put(Constants.COL_DoctorId, lAddMedicineListModel.getDoctorId());
        values.put(Constants.COL_MedicineName, lAddMedicineListModel.getMedicineName());
        values.put(Constants.COL_CTMedicineType, lAddMedicineListModel.getMedicineType());
        values.put(Constants.COL_InMorning, lAddMedicineListModel.getIsMorning());
        values.put(Constants.COL_InAfternoon, lAddMedicineListModel.getIsNoon());
        values.put(Constants.COL_InNight, lAddMedicineListModel.getIsNight());
        values.put(Constants.COL_CTFoodDependency, lAddMedicineListModel.getFoodStatus());
        values.put(Constants.COL_Quantity, lAddMedicineListModel.getQuantity());
        values.put(Constants.COL_NumberOfDays, lAddMedicineListModel.getDays());
        values.put(Constants.COL_Notes, lAddMedicineListModel.getAdditionalInfo());
        values.put(Constants.COL_CTSource, lAddMedicineListModel.getCTSource());
        values.put(Constants.COL_CTSendDataToServer, lAddMedicineListModel.getCTSendDataToServer());
        values.put(Constants.COL_IsDeleted, lAddMedicineListModel.getIsDeleted());

        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_Medicine, Constants.COL_MedicineId, lAddMedicineListModel.getMedicineID()))
        {
            db.updateWithOnConflict(Constants.TABLE_Medicine, values,
                    Constants.COL_MedicineId + " = '" + lAddMedicineListModel.getMedicineID() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            db.insert(Constants.TABLE_Medicine, null, values);
        }

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    /**
     * List all medicines with their type
     *
     * @param lAddMedicineListModel
     * @return
     */
    public ArrayList<MedicineListModel> listAll(MedicineListModel lAddMedicineListModel)
    {
        ArrayList<MedicineListModel> arrayList = new ArrayList<>();

//        String query = " SELECT * FROM " + Constants.TABLE_Medicine
//                + " WHERE " + Constants.COL_IsDeleted + " = " + Constants.FALSE
//                + " ORDER BY upper(" + Constants.COL_MedicineName + ") ASC , " + Constants.COL_CTMedicineType + " ASC ";

        String query = " SELECT TypeDisplayText , * FROM " + Constants.TABLE_Medicine
                + " INNER JOIN " + Constants.TABLE_MedicineType + " ON Medicine.CTMedicineType= MedicineType.MedicineTypeId "
                + " WHERE " + Constants.COL_IsDeleted + " = " + Constants.FALSE
                + " ORDER BY upper(" + Constants.COL_MedicineName + ") ASC , " + Constants.COL_TypeDisplayText + " ASC ";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            MedicineListModel lModel = new MedicineListModel();

            lModel.setMedicineID(cursor.getString(cursor.getColumnIndex(Constants.COL_MedicineId)));
            lModel.setDoctorId(cursor.getString(cursor.getColumnIndex(Constants.COL_DoctorId)));
            lModel.setMedicineName(cursor.getString(cursor.getColumnIndex(Constants.COL_MedicineName)));
            lModel.setMedicineType(cursor.getString(cursor.getColumnIndex(Constants.COL_CTMedicineType)));
            lModel.setIsMorning(cursor.getInt(cursor.getColumnIndex(Constants.COL_InMorning)));
            lModel.setIsNoon(cursor.getInt(cursor.getColumnIndex(Constants.COL_InAfternoon)));
            lModel.setIsNight(cursor.getInt(cursor.getColumnIndex(Constants.COL_InNight)));
            lModel.setQuantity(cursor.getString(cursor.getColumnIndex(Constants.COL_Quantity)));
            lModel.setDays(cursor.getString(cursor.getColumnIndex(Constants.COL_NumberOfDays)));
            lModel.setFoodStatus(cursor.getInt(cursor.getColumnIndex(Constants.COL_CTFoodDependency)));
            lModel.setAdditionalInfo(cursor.getString(cursor.getColumnIndex(Constants.COL_Notes)));
            lModel.setCTSource(cursor.getInt(cursor.getColumnIndex(Constants.COL_CTSource)));
            lModel.setCTSendDataToServer(cursor.getInt(cursor.getColumnIndex(Constants.COL_CTSendDataToServer)));
            lModel.setIsDeleted(cursor.getInt(cursor.getColumnIndex(Constants.COL_IsDeleted)));
            lModel.setMedicineTypeDisplayText(cursor.getString(cursor.getColumnIndex(Constants.COL_TypeDisplayText)));
            arrayList.add(lModel);
            cursor.moveToNext();
        }
        cursor.close();
        return arrayList;
    }

    /**
     * Insert/Update diagnosis against diagnosisId
     *
     * @param lAddDiagnosisListModel
     */
    public void save(DiagnosisModel lAddDiagnosisListModel)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_DiagnosisId, lAddDiagnosisListModel.getDiagnosisId());
        values.put(Constants.COL_DoctorId, lAddDiagnosisListModel.getDoctorId());
        values.put(Constants.COL_DiagnosisName, lAddDiagnosisListModel.getDiagnosisName());
        values.put(Constants.COL_DiagnosisAgeGroup, lAddDiagnosisListModel.getDiaAgeGroup());
        values.put(Constants.COL_Notes, lAddDiagnosisListModel.getNotes());
        values.put(Constants.COL_IsDeleted, lAddDiagnosisListModel.getIsDeleted());

        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_Diagnosis, Constants.COL_DiagnosisId, lAddDiagnosisListModel.getDiagnosisId()))
        {
            db.updateWithOnConflict(Constants.TABLE_Diagnosis, values,
                    Constants.COL_DiagnosisId + " = '" + lAddDiagnosisListModel.getDiagnosisId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            db.insert(Constants.TABLE_Diagnosis, null, values);
        }

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    /**
     * Insert/Update prescription against prescriptionId
     *
     * @param model
     */
    public void save(DBPrescriptionModel model)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_PrescriptionId, model.getPrescriptionId());
        values.put(Constants.COL_BPDiastolicLowerValue, model.getBPDiastolicLowerValue());
        values.put(Constants.COL_BPSystolicUpperValue, model.getBPSystolicUpperValue());
        values.put(Constants.COL_AppointmentId, model.getAppointmentId());
        values.put(Constants.COL_PatientId, model.getPatientId());
        values.put(Constants.COL_PatientName, model.getPatientName());
        values.put(Constants.COL_CTGender, model.getCTGender());
        values.put(Constants.COL_Age, model.getAge());
        values.put(Constants.COL_Notes, model.getNotes());
        values.put(Constants.COL_DoctorId, model.getDoctorId());
        values.put(Constants.COL_DiagnosisId, model.getDiagnosisId());
        values.put(Constants.COL_PrescriptionDate, model.getPrescriptionDate());
        values.put(Constants.COL_OPDClinicUniqueId, model.getOPDClinicUniqueID());
        values.put(Constants.COL_IsDeleted, model.getIsDeleted());
        values.put(Constants.COL_IsNewRecord, model.getIsNewRecord());
        values.put(Constants.COL_CTSendDataToServer, model.getSendDataToServer());

        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_Prescription, Constants.COL_PrescriptionId, model.getPrescriptionId()))
        {
            db.updateWithOnConflict(Constants.TABLE_Prescription, values,
                    Constants.COL_PrescriptionId + " = '" + model.getPrescriptionId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            db.insert(Constants.TABLE_Prescription, null, values);
        }

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    /**
     * For Existing medicine : Insert/Update prescription medicines against prescriptionId and medicineId
     * For New medicine : Insert/Update prescription medicines against prescriptionId and new medicineName
     *
     * @param model
     */
    public void save(DBPrescriptionMappingModel model)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_PrescriptionMedicineMappingId, model.getPrescriptionId());
        values.put(Constants.COL_NumberOfDays, model.getNumberOfDays());
        values.put(Constants.COL_AdditionalDetail, model.getAdditionalDetail());
        values.put(Constants.COL_PrescriptionId, model.getPrescriptionId());
        values.put(Constants.COL_MedicineId, model.getMedicineId());
        values.put(Constants.COL_MedicineName, model.getMedicineName());
        values.put(Constants.COL_InMorning, model.getInMorning());
        values.put(Constants.COL_InAfternoon, model.getInAfternoon());
        values.put(Constants.COL_InNight, model.getInNight());
        values.put(Constants.COL_CTFoodDependency, model.getCTFoodDependency());
        values.put(Constants.COL_Quantity, model.getQuantity());
        values.put(Constants.COL_IsDeleted, model.getIsDeleted());

        if (model.getMedicineId().equals(""))
        {
            //medicineID blank in case of NEW medicine given in offline mode
            if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_PrescriptionMedicineMapping, Constants.COL_PrescriptionId, model.getPrescriptionId(), Constants.COL_MedicineName, model.getMedicineName()))
            {
                db.updateWithOnConflict(Constants.TABLE_PrescriptionMedicineMapping, values,
                        Constants.COL_PrescriptionId + " = '" + model.getPrescriptionId() + "' AND "
                                + Constants.COL_MedicineName + " = '" + model.getMedicineName() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
            } else
            {
                db.insert(Constants.TABLE_PrescriptionMedicineMapping, null, values);
            }
        } else
        {
            if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_PrescriptionMedicineMapping, Constants.COL_PrescriptionId, model.getPrescriptionId(), Constants.COL_MedicineId, model.getMedicineId()))
            {
                db.updateWithOnConflict(Constants.TABLE_PrescriptionMedicineMapping, values,
                        Constants.COL_PrescriptionId + " = '" + model.getPrescriptionId() + "' AND "
                                + Constants.COL_MedicineId + " = '" + model.getMedicineId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
            } else
            {
                db.insert(Constants.TABLE_PrescriptionMedicineMapping, null, values);
            }
        }

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    /**
     * List all diagnosis
     *
     * @param dbDiagnosisModel
     * @return
     */
    public ArrayList<DiagnosisModel> listAll(DiagnosisModel dbDiagnosisModel)
    {
        ArrayList<DiagnosisModel> arrayList = new ArrayList<>();
        String query = " SELECT * FROM " + Constants.TABLE_Diagnosis
                + " ORDER BY upper(" + Constants.COL_DiagnosisName + ") ASC ,upper(" + Constants.COL_DiagnosisAgeGroup + ") ASC";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            DiagnosisModel lModel = new DiagnosisModel();
            lModel.setDiagnosisId(cursor.getString(cursor.getColumnIndex(Constants.COL_DiagnosisId)));
            lModel.setDoctorId(cursor.getString(cursor.getColumnIndex(Constants.COL_DoctorId)));
            lModel.setDiagnosisName(cursor.getString(cursor.getColumnIndex(Constants.COL_DiagnosisName)));
            lModel.setDiaAgeGroup(cursor.getString(cursor.getColumnIndex(Constants.COL_DiagnosisAgeGroup)));
            lModel.setNotes(cursor.getString(cursor.getColumnIndex(Constants.COL_Notes)));
            lModel.setIsDeleted(cursor.getInt(cursor.getColumnIndex(Constants.COL_IsDeleted)));
            arrayList.add(lModel);
            cursor.moveToNext();
        }
        cursor.close();
        return arrayList;
    }

    public ArrayList<DBAppointmentModel> listAll(DBAppointmentModel dbAppointmentModel, String TxnOPDUniqueID)
    {
        ArrayList<DBAppointmentModel> arrayList = new ArrayList<>();
        String query;
//        query = " SELECT * FROM " + Constants.TABLE_APPOINTMENT
//                + " WHERE " + Constants.COL_APPTxnOPDUniqueId + " = '" + TxnOPDUniqueID
//                + "' ORDER BY " + Constants.COL_APPCTStatus + " DESC , " + Constants.COL_APPSortOrder + " ASC ";

//        query = " SELECT Patient.DisplayPicture,Appointment.* FROM " + Constants.TABLE_APPOINTMENT
//                + " INNER JOIN " + Constants.TABLE_Patient + " ON Appointment.PatientId=Patient.PatientId "
//                + " WHERE " + Constants.COL_APPTxnOPDUniqueId + " = '" + TxnOPDUniqueID
//                + "' ORDER BY "
//                + Constants.COL_APPCTStatus + " DESC , "
//                + Constants.COL_APPSortOrder + " ASC ,  "
//                + Constants.COL_APPTokenValue + " ASC ";

        query = " SELECT Patient.DisplayPicture,Patient.PatientName as patientName,Patient.ContactNumber as contactNumber,Patient.Age as age,Appointment.* "
                + " FROM " + Constants.TABLE_APPOINTMENT
                + " INNER JOIN " + Constants.TABLE_Patient + " ON Appointment.PatientId=Patient.PatientId "
                + " WHERE " + Constants.COL_APPTxnOPDUniqueId + " = '" + TxnOPDUniqueID
                + "' ORDER BY "
                + Constants.COL_APPCTStatus + " DESC , "
                + Constants.COL_APPSortOrder + " ASC ,  "
                + Constants.COL_APPTokenValue + " ASC ";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            DBAppointmentModel model = new DBAppointmentModel();
//            model.setAge(cursor.getInt(cursor.getColumnIndex(Constants.COL_APPAge)));
            model.setAge(cursor.getInt(cursor.getColumnIndex("age")));
            model.setCTGender(cursor.getInt(cursor.getColumnIndex(Constants.COL_APPCTGender)));
            model.setCTStatus(cursor.getInt(cursor.getColumnIndex(Constants.COL_APPCTStatus)));
            model.setMemberId(cursor.getString(cursor.getColumnIndex(Constants.COL_APPMemberId)));
            model.setAppointmentId(cursor.getString(cursor.getColumnIndex(Constants.COL_APPUniqueId)));
            model.setUpdatedBy(cursor.getString(cursor.getColumnIndex(Constants.COL_APPUpdatedBy)));
            model.setCreatedBy(cursor.getString(cursor.getColumnIndex(Constants.COL_APPCreatedBy)));
            model.setIsDeleted(cursor.getString(cursor.getColumnIndex(Constants.COL_APPIsDeleted)));
            model.setCreatedDtm(cursor.getString(cursor.getColumnIndex(Constants.COL_APPCreatedDtm)));
            model.setCTBookedBy(cursor.getInt(cursor.getColumnIndex(Constants.COL_APPCTBookedBy)));
            model.setUpdatedDtm(cursor.getString(cursor.getColumnIndex(Constants.COL_APPUpdatedDtm)));
            model.setTokenValue(cursor.getString(cursor.getColumnIndex(Constants.COL_APPTokenValue)));
            model.setAttendantId(cursor.getString(cursor.getColumnIndex(Constants.COL_APPAttendantId)));

//            model.setPatientName(cursor.getString(cursor.getColumnIndex(Constants.COL_APPPatientName)));
            model.setPatientName(cursor.getString(cursor.getColumnIndex("patientName")));

            model.setCTVisitType(cursor.getInt(cursor.getColumnIndex(Constants.COL_APPCTVisitType)));
//            model.setContactNumber(cursor.getString(cursor.getColumnIndex(Constants.COL_APPContactNumber)));
            model.setContactNumber(cursor.getString(cursor.getColumnIndex("contactNumber")));
            model.setSortorder(cursor.getInt(cursor.getColumnIndex(Constants.COL_APPSortOrder)));
            model.setAppointmentAddedDtmUTC(cursor.getString(cursor.getColumnIndex(Constants.COL_APPAppointmentAddedDtmUTC)));
            model.setIsNewRecord(cursor.getInt(cursor.getColumnIndex(Constants.COL_APPIsNewRecord)));
            model.setCTDataSentToServerStatus(cursor.getInt(cursor.getColumnIndex(Constants.COL_APPCTDataSentToServerStatus)));
            model.setRequestReceivedAtDtm(cursor.getString(cursor.getColumnIndex(Constants.COL_APPRequestReceivedAtDtm)));
            model.setPatientId(cursor.getString(cursor.getColumnIndex(Constants.COL_PatientId)));
            model.setDisplayPicture(cursor.getString(cursor.getColumnIndex(Constants.COL_DisplayPicture)));
            model.setIsFeeApplicable(cursor.getInt(cursor.getColumnIndex(Constants.COL_IsFeeApplicable)));
            model.setFeeAmount(cursor.getInt(cursor.getColumnIndex(Constants.COL_FeePaidAmount)));
            model.setIsDueAmount(cursor.getInt(cursor.getColumnIndex(Constants.COL_IsBalanceDue)));
            model.setDueAmount(cursor.getInt(cursor.getColumnIndex(Constants.COL_BalanceAmount)));
            arrayList.add(model);
            cursor.moveToNext();
        }

        cursor.close();
        return arrayList;
    }

    /**
     * Insert/Update diagnosis medicines against diagnosisId and medicineId
     *
     * @param model
     */
    public void save(DBDiagnosisMedicineMappingModel model)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_DiagnosisId, model.getDiagnosisUniqueId());
        values.put(Constants.COL_MedicineId, model.getMedicineID());
        values.put(Constants.COL_MedicineName, model.getMedicineName());
        values.put(Constants.COL_InMorning, model.getIsMorning());
        values.put(Constants.COL_InAfternoon, model.getIsNoon());
        values.put(Constants.COL_InNight, model.getIsNight());
        values.put(Constants.COL_CTFoodDependency, model.getFoodDependency());
        values.put(Constants.COL_Quantity, model.getQuantity());
        values.put(Constants.COL_NumberOfDays, model.getDays());
        values.put(Constants.COL_AdditionalDetail, model.getAdditionalInfo());

        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_DiagnosisMedicineMapping, Constants.COL_DiagnosisId, model.getDiagnosisUniqueId(), Constants.COL_MedicineId, model.getMedicineID()))
        {
//            db.updateWithOnConflict(Constants.TABLE_DiagnosisMedicineMapping, values,
//                    Constants.COL_DiagnosisId + " = '" + model.getDiagnosisUniqueId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);

            db.updateWithOnConflict(Constants.TABLE_DiagnosisMedicineMapping, values,
                    Constants.COL_DiagnosisId + " = '" + model.getDiagnosisUniqueId() + "' AND "
                            + Constants.COL_MedicineId + " = '" + model.getMedicineID() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            db.insert(Constants.TABLE_DiagnosisMedicineMapping, null, values);
        }

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    /**
     * Save medicine types
     *
     * @param model
     */
    public void save(DBMedicineTypeModel model)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_MedicineTypeId, model.getMedicineTypeId());
        values.put(Constants.COL_TypeDisplayText, model.getTypeDisplayText());

        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_MedicineType, Constants.COL_MedicineTypeId, "" + model.getMedicineTypeId()))
        {
//            db.updateWithOnConflict(Constants.TABLE_MedicineType, values,
//                    Constants.COL_MedicineTypeId + " = '" + model.getMedicineTypeId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            db.insert(Constants.TABLE_MedicineType, null, values);
        }

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    /**
     * List all medicine types
     *
     * @param dbMedicineTypeModel
     * @return
     */
    public ArrayList<DBMedicineTypeModel> listAll(DBMedicineTypeModel dbMedicineTypeModel)
    {
        ArrayList<DBMedicineTypeModel> arrayList = new ArrayList<>();
        String query = " SELECT * FROM " + Constants.TABLE_MedicineType
                + " ORDER BY upper(" + Constants.COL_TypeDisplayText + ") ASC ";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            DBMedicineTypeModel lModel = new DBMedicineTypeModel();
            lModel.setMedicineTypeId(cursor.getInt(cursor.getColumnIndex(Constants.COL_MedicineTypeId)));
            lModel.setTypeDisplayText(cursor.getString(cursor.getColumnIndex(Constants.COL_TypeDisplayText)));
            arrayList.add(lModel);
            cursor.moveToNext();
        }
        cursor.close();
        return arrayList;
    }

    public boolean isAppointmentAllowed(String opdUniqueID)
    {
        //Returns False, if user is going to take Appointment more than Max Allowed Appointment
        boolean flag = false;
        String Query = "SELECT MaxAppointment, " +
                "(SELECT COUNT(*) FROM Appointment WHERE TxnOPDUniqueId='" + opdUniqueID + "') AS AppointmentCount" +
                " FROM TxnOPD WHERE TxnOPD.TxnOPDUniqueId='" + opdUniqueID + "'";
        Cursor cursor = db.rawQuery(Query, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0)
        {
            int MaxApp = cursor.getInt(cursor.getColumnIndex("MaxAppointment"));
            int AppointmentCount = cursor.getInt(cursor.getColumnIndex("AppointmentCount"));
            if (AppointmentCount < MaxApp)
            {
                flag = true;
            } else
            {
                flag = false;
            }
        }
        cursor.close();
        return flag;
    }

    public int isOPDActive(String opdUniqueID)
    {
        int opdStatus = 0;
        String Query = "SELECT " + Constants.COL_TxnCTOPDStatus
                + " FROM " + Constants.TABLE_TxnOPD
                + " WHERE " + Constants.COL_TxnOPDUniqueId + " = '" + opdUniqueID + "'";
        Cursor cursor = db.rawQuery(Query, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0)
        {
            opdStatus = cursor.getInt(cursor.getColumnIndex(Constants.COL_TxnCTOPDStatus));
        }
        cursor.close();
        return opdStatus;
    }

    /**
     * Check availability of data based on that perform Insert or Update
     * dbfield = column name ,fieldValue = column value
     *
     * @param TableName
     * @param dbfield
     * @param fieldValue
     * @return
     */
    public boolean CheckIsDataAlreadyInDBorNot(String TableName, String dbfield, String fieldValue)
    {
        String Query = "SELECT * FROM " + TableName + " WHERE " + dbfield + " = '" + fieldValue + "' ";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0)
        {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * Check availability of data based on that perform Insert or Update
     * dbfield = column name ,fieldValue = column value
     *
     * @param TableName
     * @param dbfield
     * @param fieldValue
     * @return
     */
    public boolean CheckIsDataAlreadyInDBorNot(String TableName, String dbfield, String fieldValue, String dbfield1, String fieldValue1)
    {
        String Query = "SELECT * FROM " + TableName + " WHERE " + dbfield + " = '" + fieldValue + "' AND "
                + dbfield1 + " = '" + fieldValue1 + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0)
        {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * Check weather table contains data or not
     *
     * @param tableName
     * @return
     */
    public boolean CheckDataIsAvailable(String tableName)
    {
        String Query = "SELECT * FROM " + tableName;
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0)
        {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
//        return ((db.rawQuery("SELECT * FROM " + tableName, null).getCount() > 0) ? true : false);
    }

    public int getLastGeneratedToken(String txnOPDUniqueID)
    {
        //get Lastgenerated Token value from TxnOPD table
        int LastgeneratedToken = 1;
        String selectQuery = " SELECT " + Constants.COL_TxnLastGeneratedTokenValue
                + " FROM " + Constants.TABLE_TxnOPD
                + " WHERE " + Constants.COL_TxnOPDUniqueId + "='" + txnOPDUniqueID + "' ";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null)
        {
            if (c.getCount() > 0)
            {
                c.moveToFirst();
                LastgeneratedToken = c.getInt(0);
                LastgeneratedToken = LastgeneratedToken + 1;
            }
        }
        c.close();
        return LastgeneratedToken;
    }

//    public Cursor getStationCursor(String args)
//    {
//        String sqlQuery = "";
//        Cursor result = null;
//
//        sqlQuery = " SELECT _id" + ", PatientName ";
//        sqlQuery += " FROM Appointment";
//        sqlQuery += " WHERE PatientName LIKE '%" + args + "%' ";
//        sqlQuery += " ORDER BY PatientName";
////        sqlQuery = "SELECT " + Constants.COL_APPPatientName + " FROM " + Constants.TABLE_APPOINTMENT + " WHERE " + Constants.COL_APPPatientName + " LIKE '%" + args + "%'";
//        if (db == null)
//        {
//            getWriteable();
//
//        }
//
//        if (db != null)
//        {
//            result = db.rawQuery(sqlQuery, null);
//        }
//        return result;
//    }

    /**
     * function uses
     *
     * @param group
     * @return
     */
    public ArrayList<PatientsListmodel> getPatientNames(String group)
    {
        ArrayList<PatientsListmodel> arrayList = new ArrayList<>();
        String selectQuery = "";
        if (group.length() > 0)
        {
//            selectQuery = " SELECT " + Constants.COL_PatientName + " , " + Constants.COL_PatientId
//                    + " FROM " + Constants.TABLE_Patient + " WHERE " + Constants.COL_Group + " = '" + group + "'"
//                    + " ORDER BY " + Constants.COL_PatientName + " ASC ";
            selectQuery = " SELECT * "
                    + " FROM " + Constants.TABLE_Patient + " WHERE " + Constants.COL_Group + " = '" + group + "'"
                    + " ORDER BY " + Constants.COL_PatientName + " ASC ";
        } else
        {
//            selectQuery = " SELECT " + Constants.COL_PatientName + " , " + Constants.COL_PatientId
//                    + " FROM " + Constants.TABLE_Patient
//                    + " ORDER BY " + Constants.COL_PatientName + " ASC ";
            selectQuery = " SELECT * "
                    + " FROM " + Constants.TABLE_Patient
                    + " ORDER BY " + Constants.COL_PatientName + " ASC ";
        }

        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        while (!c.isAfterLast())
        {
            PatientsListmodel listmodel = new PatientsListmodel();
            listmodel.setPatientId(c.getString(c.getColumnIndex(Constants.COL_PatientId)));
            listmodel.setPatientName(c.getString(c.getColumnIndex(Constants.COL_PatientName)));
            listmodel.setAge(c.getInt(c.getColumnIndex(Constants.COL_Age)));
            listmodel.setAddress(c.getString(c.getColumnIndex(Constants.COL_Address)));
            listmodel.setGender(c.getInt(c.getColumnIndex(Constants.COL_CTGender)));
            listmodel.setContactNumber(c.getString(c.getColumnIndex(Constants.COL_ContactNumber)));
            arrayList.add(listmodel);
            c.moveToNext();
        }
        c.close();
        return arrayList;
    }

    public ArrayList<PatientsListmodel> listAll(PatientsListmodel model, String patientId)
    {
        ArrayList<PatientsListmodel> arrayList = new ArrayList<>();
        String query = "";
        if (patientId != null)
        {
            query = " SELECT * FROM " + Constants.TABLE_Patient
                    + " WHERE " + Constants.COL_PatientId + " = '" + patientId + "'";
        } else
        {
            query = " SELECT * FROM " + Constants.TABLE_Patient;
        }
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            PatientsListmodel lModel = new PatientsListmodel();
            lModel.setAge(cursor.getInt(cursor.getColumnIndex(Constants.COL_Age)));
            lModel.setGroup(cursor.getString(cursor.getColumnIndex(Constants.COL_Group)));
            lModel.setGender(cursor.getInt(cursor.getColumnIndex(Constants.COL_CTGender)));
            lModel.setAddress(cursor.getString(cursor.getColumnIndex(Constants.COL_Address)));
            lModel.setPatientId(cursor.getString(cursor.getColumnIndex(Constants.COL_PatientId)));
            lModel.setPatientName(cursor.getString(cursor.getColumnIndex(Constants.COL_PatientName)));
            lModel.setContactNumber(cursor.getString(cursor.getColumnIndex(Constants.COL_ContactNumber)));
            lModel.setDisplayPicture(cursor.getString(cursor.getColumnIndex(Constants.COL_DisplayPicture)));
            arrayList.add(lModel);
            cursor.moveToNext();
        }
        cursor.close();
        return arrayList;
    }

    public ArrayList getMedicineList()
    {
        ArrayList<MedicineListModel> arrayList = new ArrayList<>();

//        String lQuery = " SELECT " + Constants.COL_MedicineName + " , " + Constants.COL_MedicineId
//                + " FROM " + Constants.TABLE_Medicine + " WHERE " + Constants.COL_IsDeleted + " = " + Constants.FALSE;

        String lQuery = " SELECT " + Constants.COL_MedicineName + " , " + Constants.COL_MedicineId + " , " + Constants.COL_TypeDisplayText
                + " FROM " + Constants.TABLE_Medicine
                + " INNER JOIN " + Constants.TABLE_MedicineType + " ON Medicine.CTMedicineType= MedicineType.MedicineTypeId  "
                + " WHERE " + Constants.COL_IsDeleted + " = " + Constants.FALSE
                + " ORDER BY upper(Medicine.MedicineName) asc , TypeDisplayText ASC  ";

        Cursor lCursor = db.rawQuery(lQuery, null);
        lCursor.moveToNext();
        if (lCursor.getCount() > 0)
        {
            while (!lCursor.isAfterLast())
            {
                MedicineListModel lModel = new MedicineListModel();
                lModel.setMedicineID(lCursor.getString(lCursor.getColumnIndex(Constants.COL_MedicineId)));
                lModel.setMedicineName(lCursor.getString(lCursor.getColumnIndex(Constants.COL_MedicineName)));
                lModel.setMedicineTypeDisplayText(lCursor.getString(lCursor.getColumnIndex(Constants.COL_TypeDisplayText)));
                arrayList.add(lModel);
                lCursor.moveToNext();
            }
        }
        lCursor.close();
        return arrayList;
    }

    public ArrayList getMedicineDetail(String medicineId)
    {
        ArrayList<MedicineListModel> arrayList = new ArrayList<>();
        String lQuery = " SELECT * FROM " + Constants.TABLE_Medicine + " WHERE " + Constants.COL_MedicineId + " = " + "'" + medicineId + "'";
        Cursor lCursor = db.rawQuery(lQuery, null);
        lCursor.moveToNext();
        while (!lCursor.isAfterLast())
        {
            MedicineListModel lModel = new MedicineListModel();
            lModel.setMedicineName(lCursor.getString(lCursor.getColumnIndex(Constants.COL_MedicineName)));
            lModel.setMedicineType(lCursor.getString(lCursor.getColumnIndex(Constants.COL_CTMedicineType)));
            lModel.setIsMorning(lCursor.getInt(lCursor.getColumnIndex(Constants.COL_InMorning)));
            lModel.setIsNoon(lCursor.getInt(lCursor.getColumnIndex(Constants.COL_InAfternoon)));
            lModel.setIsNight(lCursor.getInt(lCursor.getColumnIndex(Constants.COL_InNight)));
            lModel.setFoodStatus(lCursor.getInt(lCursor.getColumnIndex(Constants.COL_CTFoodDependency)));
            lModel.setQuantity(lCursor.getString(lCursor.getColumnIndex(Constants.COL_Quantity)));
            lModel.setDays(lCursor.getString(lCursor.getColumnIndex(Constants.COL_NumberOfDays)));
            lModel.setAdditionalInfo(lCursor.getString(lCursor.getColumnIndex(Constants.COL_Notes)));

            arrayList.add(lModel);
            lCursor.moveToNext();
        }

        lCursor.close();
        return arrayList;
    }

    public ArrayList getDiagnosisMedicine(String diagnosisId)
    {
        ArrayList<MedicineListModel> arrayList = new ArrayList<>();
        String lQuery = " SELECT * FROM " + Constants.TABLE_DiagnosisMedicineMapping + " WHERE " + Constants.COL_DiagnosisId + " = '" + diagnosisId + "'";
        Cursor lCursor = db.rawQuery(lQuery, null);
        if (lCursor.getCount() > 0)
        {
            lCursor.moveToNext();
            while (!lCursor.isAfterLast())
            {
                MedicineListModel lModel = new MedicineListModel();
                lModel.setMedicineID(lCursor.getString(lCursor.getColumnIndex(Constants.COL_MedicineId)));
                lModel.setMedicineName(lCursor.getString(lCursor.getColumnIndex(Constants.COL_MedicineName)));
                lModel.setIsMorning(lCursor.getInt(lCursor.getColumnIndex(Constants.COL_InMorning)));
                lModel.setIsNoon(lCursor.getInt(lCursor.getColumnIndex(Constants.COL_InAfternoon)));
                lModel.setIsNight(lCursor.getInt(lCursor.getColumnIndex(Constants.COL_InNight)));
                lModel.setFoodStatus(lCursor.getInt(lCursor.getColumnIndex(Constants.COL_CTFoodDependency)));
                lModel.setQuantity(lCursor.getString(lCursor.getColumnIndex(Constants.COL_Quantity)));
                lModel.setDays(lCursor.getString(lCursor.getColumnIndex(Constants.COL_NumberOfDays)));
                lModel.setAdditionalInfo(lCursor.getString(lCursor.getColumnIndex(Constants.COL_AdditionalDetail)));

                arrayList.add(lModel);
                lCursor.moveToNext();
            }
        }
        lCursor.close();
        return arrayList;
    }

    public void save(AllergicToModel model)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_PatientId, model.getPatientId());
        values.put(Constants.COL_AllergyText, model.getAllergiName());
        values.put(Constants.COL_AllergyId, model.getAllergyId());
        values.put(Constants.COL_IsDeleted, model.getIsDeleted());

//        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_Allergy, Constants.COL_AllergyText, model.getAllergiName()))
        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_Allergy, Constants.COL_AllergyText, model.getAllergiName(), Constants.COL_PatientId, model.getPatientId()))
        {
            //---UPDATE
            db.updateWithOnConflict(Constants.TABLE_Allergy, values,
                    Constants.COL_AllergyText + " = '" + model.getAllergiName()
                            + "' AND " + Constants.COL_PatientId + " = '" + model.getPatientId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            //---ADD
            db.insert(Constants.TABLE_Allergy, null, values);
        }

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    public ArrayList<AllergicToModel> listAll(AllergicToModel model, String patientID)
    {
        ArrayList<AllergicToModel> arrayList = new ArrayList<>();
        String query = " SELECT * FROM " + Constants.TABLE_Allergy
                + " WHERE " + Constants.COL_PatientId + " = '" + patientID + "'";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                AllergicToModel lModel = new AllergicToModel();
                lModel.setAllergyId(cursor.getString(cursor.getColumnIndex(Constants.COL_AllergyId)));
                lModel.setPatientId(cursor.getString(cursor.getColumnIndex(Constants.COL_PatientId)));
                lModel.setAllergiName(cursor.getString(cursor.getColumnIndex(Constants.COL_AllergyText)));
                lModel.setIsDeleted(cursor.getInt(cursor.getColumnIndex(Constants.COL_IsDeleted)));
                arrayList.add(lModel);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return arrayList;
    }

    public void save(DBPatientModel model)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_PatientId, model.getPatientId());
        values.put(Constants.COL_PatientName, model.getPatientName());
        values.put(Constants.COL_Group, model.getGroup());
        values.put(Constants.COL_ContactNumber, model.getContactNumber());
        values.put(Constants.COL_CTGender, model.getCTGender());
        values.put(Constants.COL_Age, model.getAge());
        values.put(Constants.COL_AgeAsOnDt, model.getAgeAsOnDt());
        values.put(Constants.COL_Address, model.getAddress());
        values.put(Constants.COL_DisplayPicture, model.getDisplayPicture());
        values.put(Constants.COL_ThumbnailPicture, model.getThumbnailPicture());
        values.put(Constants.COL_LastConsultationDate, model.getLastConsultationDate());
        values.put(Constants.COL_IsDeleted, model.getIsDeleted());
        values.put(Constants.COL_CTSource, model.getSource());
        values.put(Constants.COL_CTSendDataToServer, model.getSendDataToServer());
        values.put(Constants.COL_IsNewRecord, model.getIsNewRecord());

        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_Patient, Constants.COL_PatientId, model.getPatientId()))
        {
            //---UPDATE
            db.updateWithOnConflict(Constants.TABLE_Patient, values,
                    Constants.COL_PatientId + " = '" + model.getPatientId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            //---ADD
            db.insert(Constants.TABLE_Patient, null, values);
        }
        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    public void save(MedicalRecordModel model)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_MedicalRecordId, model.getMedicalRecordId());
        values.put(Constants.COL_MedicalRecordName, model.getMedicalRecordName());
        values.put(Constants.COL_CTRecordType, model.getType());
        values.put(Constants.COL_RecordDate, model.getDate());
        values.put(Constants.COL_Notes, model.getNotes());
        values.put(Constants.COL_PatientId, model.getPatientID());
        values.put(Constants.COL_AddedFrom, model.getAddedFrom());

        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_MedicalRecord, Constants.COL_MedicalRecordId, model.getMedicalRecordId()))
        {
            //---UPDATE
            db.updateWithOnConflict(Constants.TABLE_MedicalRecord, values,
                    Constants.COL_MedicalRecordId + " = '" + model.getMedicalRecordId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            //---ADD
            db.insert(Constants.TABLE_MedicalRecord, null, values);
        }

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

    public void save(MedicalRecordImagesModel model)
    {
        //db.beginTransaction(); 

        ContentValues values = new ContentValues();
        values.put(Constants.COL_MedicalRecordFileId, model.getMedicineRecordFileUniqueId());
        values.put(Constants.COL_FileCaption, model.getFileCaption());
        values.put(Constants.COL_DisplaySortOrder, model.getDisplaySortOrder());
        values.put(Constants.COL_DisplayPicture, model.getDisplayPicture());
        values.put(Constants.COL_ThumbnailPicture, model.getThumbnailPicture());
        values.put(Constants.COL_MedicalRecordId, model.getMedicineRecordUniqueId());
        values.put(Constants.COL_UploadedOnDtm, model.getUploadedOnDtm());

        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_MedicalRecordFile, Constants.COL_MedicalRecordFileId, model.getMedicineRecordFileUniqueId()))
        {
            //---UPDATE
            db.updateWithOnConflict(Constants.TABLE_MedicalRecordFile, values,
                    Constants.COL_MedicalRecordFileId + " = '" + model.getMedicineRecordFileUniqueId() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
        } else
        {
            //---ADD
            db.insert(Constants.TABLE_MedicalRecordFile, null, values);
        }

        //db.setTransactionSuccessful();
        //db.endTransaction();
    }

//    public void save(DBTxansModel model)
//    {
//        ContentValues values = new ContentValues();
//        values.put(Constants.COL_TransactionId, model.getTxnsID());
//        values.put(Constants.COL_PatientId, model.getPatientID());
//        values.put(Constants.COL_DoctorId, model.getDoctorID());
//        values.put(Constants.COL_CTTransactionType, model.getTxnsType());
//        values.put(Constants.COL_TransactionDt, model.getTxnsDate());
//        values.put(Constants.COL_Amount, model.getTxnsAmount());
//        values.put(Constants.COL_Remarks, model.getTxnsRemarks());
//        values.put(Constants.COL_IsDeleted, model.getTxnsIsDeleted());
//        values.put(Constants.COL_CTSendDataToServer, model.getSendDataToServer());
//
//        if (CheckIsDataAlreadyInDBorNot(Constants.TABLE_Accounts, Constants.COL_TransactionId, model.getTxnsID()))
//        {
//            db.updateWithOnConflict(Constants.TABLE_Accounts, values,
//                    Constants.COL_TransactionId + " = '" + model.getTxnsID() + "'", null, SQLiteDatabase.CONFLICT_REPLACE);
//        } else
//        {
//            db.insert(Constants.TABLE_Accounts, null, values);
//        }
//    }

    /**
     * function to return age group
     *
     * @return
     */
    public ArrayList<DiagnosisModel> getAgeGrp()
    {
        ArrayList<DiagnosisModel> arrayList = new ArrayList<>();

        String lQuery = " SELECT DISTINCT " + Constants.COL_DiagnosisAgeGroup + " FROM " + Constants.TABLE_Diagnosis;
        Cursor lCursor = db.rawQuery(lQuery, null);
        lCursor.moveToNext();
        if (lCursor.getCount() > 0)
        {
            while (!lCursor.isAfterLast())
            {
                DiagnosisModel lModel = new DiagnosisModel();
                lModel.setDiaAgeGroup(lCursor.getString(lCursor.getColumnIndex(Constants.COL_DiagnosisAgeGroup)));
                arrayList.add(lModel);
                lCursor.moveToNext();
            }
        }
        lCursor.close();
        return arrayList;
    }

    public String[] listAllGroups()
    {
//        String query = " SELECT DISTINCT " + Constants.COL_Group
//                + " FROM " + Constants.TABLE_Patient + " WHERE " + Constants.COL_Group + " <> 'No Group'";
        String query = " SELECT DISTINCT " + Constants.COL_Group
                + " FROM " + Constants.TABLE_Patient + " WHERE ifnull(length(" + Constants.COL_Group + "), 0) <> 0 ";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        String[] mgrp = new String[c.getCount()];
        if (c.getCount() > 0)
        {
            while (!c.isAfterLast())
            {
                mgrp[c.getPosition()] = c.getString(c.getColumnIndex(Constants.COL_Group));
                c.moveToNext();
            }
        }
        c.close();
        return mgrp;
    }

    public boolean isOPDClinicAndAppointmentDataAvailable()
    {
//        boolean dataAvailable_TABLE_OPDClinic = false;
//        boolean dataAvailable_TABLE_APPOINTMENT = false;
//        if (CheckDataIsAvailable(Constants.TABLE_OPDClinic))
//        {
//            dataAvailable_TABLE_OPDClinic = true;
//        } else if (CheckDataIsAvailable(Constants.TABLE_APPOINTMENT))
//        {
//            dataAvailable_TABLE_APPOINTMENT = true;
//        }

        if (CheckDataIsAvailable(Constants.TABLE_OPDClinic) && (CheckDataIsAvailable(Constants.TABLE_TxnOPD)))
            return true;
        else return false;

    }

    private String addIntegerColoum(String table, String coloum, int defaultVal)
    {
        return "ALTER TABLE " + table + " ADD COLUMN "
                + coloum + " INTEGER DEFAULT " + defaultVal;
    }

    private String addTextColoum(String table, String coloum)
    {
        return "ALTER TABLE " + table + " ADD COLUMN "
                + coloum + " TEXT  ";
    }

    public void deleteAllTables()
    {
        clearDatabase(Constants.TABLE_TxnOPD);
        clearDatabase(Constants.TABLE_Patient);
        clearDatabase(Constants.TABLE_Allergy);
        clearDatabase(Constants.TABLE_Medicine);
        clearDatabase(Constants.TABLE_OPDClinic);
        clearDatabase(Constants.TABLE_Diagnosis);
        clearDatabase(Constants.TABLE_APPOINTMENT);
//        clearDatabase(Constants.CREATE_TABLE_MEDICINETYPE);
        clearDatabase(Constants.TABLE_Notification);
        clearDatabase(Constants.TABLE_Prescription);
        clearDatabase(Constants.TABLE_MedicalRecord);
        clearDatabase(Constants.TABLE_MedicalRecordFile);
        clearDatabase(Constants.TABLE_DiagnosisMedicineMapping);
        clearDatabase(Constants.TABLE_PrescriptionMedicineMapping);

        clearDatabase(Constants.TABLE_TempPrescMedicine);
        clearDatabase(Constants.TABLE_Accounts);
    }
}
