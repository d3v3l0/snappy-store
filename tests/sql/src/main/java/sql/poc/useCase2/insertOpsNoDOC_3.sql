set schema CDSDBA;

-- insert into xml_doc_3 is handled in the tests
-- need index entries for enterpriseProfile(20), nationalAccount(65), expressApplyDiscountDetail(79), accountContact(25) 
  
-- /enterpriseProfile/accountNumber 
insert into xml_idx_20_1 
   (IDX_COL1, 
   XML_DOC_ID_NBR, 
   CREATE_MINT_CD, 
   LAST_UPDATE_SYSTEM_NM, 
   LAST_UPDATE_TMSTP) 
   values ('100000001', 
   1111111111111111111, 
   '1', 
   'APP4004', 
   CURRENT_TIMESTAMP); 
  
  
  
------------------------------------------ 
-- accountContact 
insert into xml_doc_3 
   (XML_DOC_ID_NBR, 
   STRUCTURE_ID_NBR, 
   CREATE_MINT_CD, 
   MSG_PAYLOAD_QTY, 
   MSG_PAYLOAD1_IMG, 
   MSG_PAYLOAD2_IMG, 
   MSG_PAYLOAD_SIZE_NBR, 
   MSG_PURGE_DT, 
   DELETED_FLG, 
   LAST_UPDATE_SYSTEM_NM, 
   LAST_UPDATE_TMSTP, 
   MSG_MAJOR_VERSION_NBR, 
   MSG_MINOR_VERSION_NBR, 
   OPT_LOCK_TOKEN_NBR, 
   PRESET_DICTIONARY_ID_NBR) 
   values (1111111111111111112, 
   25, 
   '1', 
   1, 
   CAST(X'12345678452984560289456029847609487234785012934857109348156034650234560897628900985760289207856027895602785608560786085602857602985760206106110476191087345601456105610478568347562686289765927868972691785634975604562056104762978679451308956205620437861508561034756028475180756917856190348756012876510871789546913485620720476107856479238579385923847934' AS BLOB), 
   NULL, 
   350, 
   NULL, 
   'N', 
   'APP4004', 
   CURRENT_TIMESTAMP, 
   0, 
   5, 
   1, 
   0); 
  
-- /accountContact/accountType/accountId/number 
-- /accountContact/accountType/accountId/opco 
-- /accountContact/contactTypeCode 
-- /accountContact/contactDocumentId 
-- /accountContact/contactBusinessId 
insert into xml_idx_25_2 
   (IDX_COL1, 
   IDX_COL2, 
   IDX_COL3, 
   IDX_COL4, 
   IDX_COL5, 
   XML_DOC_ID_NBR, 
   CREATE_MINT_CD, 
   LAST_UPDATE_SYSTEM_NM, 
   LAST_UPDATE_TMSTP) 
   values ('100000001', 
   'FX', 
   'PP', 
   1234567891234545667, 
   '01', 
   1111111111111111112, 
   '1', 
   'APP4004', 
   CURRENT_TIMESTAMP); 
insert into xml_idx_25_2 
  (IDX_COL1, 
   IDX_COL2, 
   IDX_COL3, 
   IDX_COL4, 
   IDX_COL5, 
   XML_DOC_ID_NBR, 
   CREATE_MINT_CD, 
   LAST_UPDATE_SYSTEM_NM, 
   LAST_UPDATE_TMSTP) 
   values ('100000001', 
   'FX', 
   'PA', 
   1234567891234545667, 
   '01', 
   1111111111111111112, 
   '1', 
   'APP4004', 
   CURRENT_TIMESTAMP); 
insert into xml_idx_25_2 
   (IDX_COL1, 
   IDX_COL2, 
   IDX_COL3, 
   IDX_COL4, 
   IDX_COL5, 
   XML_DOC_ID_NBR, 
   CREATE_MINT_CD, 
   LAST_UPDATE_SYSTEM_NM, 
   LAST_UPDATE_TMSTP) 
   values ('100000001', 
   'FX', 
  'PBA', 
   1234567891234545667, 
   '01', 
   1111111111111111112, 
   '1', 
   'APP4004', 
   CURRENT_TIMESTAMP); 
insert into xml_idx_25_2 
   (IDX_COL1, 
   IDX_COL2, 
   IDX_COL3, 
   IDX_COL4, 
   IDX_COL5, 
   XML_DOC_ID_NBR, 
   CREATE_MINT_CD, 
   LAST_UPDATE_SYSTEM_NM, 
   LAST_UPDATE_TMSTP) 
   values ('100000001', 
   'FX', 
   'PS', 
   1234567891234545667, 
   '01', 
   1111111111111111112, 
   '1', 
   'APP4004', 
   CURRENT_TIMESTAMP); 
  
---------------------------------------------------------- 
-- nationalAccount 
insert into xml_doc_3 
   (XML_DOC_ID_NBR, 
   STRUCTURE_ID_NBR, 
   CREATE_MINT_CD, 
   MSG_PAYLOAD_QTY, 
   MSG_PAYLOAD1_IMG, 
   MSG_PAYLOAD2_IMG, 
   MSG_PAYLOAD_SIZE_NBR, 
   MSG_PURGE_DT, 
   DELETED_FLG, 
   LAST_UPDATE_SYSTEM_NM, 
   LAST_UPDATE_TMSTP, 
   MSG_MAJOR_VERSION_NBR, 
   MSG_MINOR_VERSION_NBR, 
   OPT_LOCK_TOKEN_NBR, 
   PRESET_DICTIONARY_ID_NBR) 
   values (1111111111111111113, 
   65, 
   '1', 
   1, 
   CAST(X'12345678452984560289456029847609487234785012934857109348156034650234560897628900985760289207856027895602785608560786085602857602985760206106110476191087345601456105610478568347562686289765927868972691785634975604562056104762978679451308956205620437861508561034756028475180756917856190348756012876510871789546913485620720476107856479238579385923847934' AS BLOB), 
   NULL, 
   350, 
   NULL, 
   'N', 
   'APP4004', 
   CURRENT_TIMESTAMP, 
   0, 
   5, 
   1, 
   0); 
  
-- /nationalAccount/accountId/number 
-- /nationalAccount/accountId/opco 
-- /nationalAccount/nationalAccountDetail/membershipEffDateTime 
-- /nationalAccount/nationalAccountDetail/membershipExpDateTime 
-- /nationalAccount/nationalAccountDetail/nationalAccountCompanyCd 
-- /nationalAccount/nationalAccountDetail/nationalAccountNbr 
-- /nationalAccount/nationalAccountDetail/nationalPriorityCd 
-- /nationalAccount/nationalAccountDetail/nationalSubgroupNbr 
insert into xml_idx_65_1 
   (IDX_COL1, 
   IDX_COL2, 
   IDX_COL3, 
   IDX_COL4, 
   IDX_COL5, 
   IDX_COL6, 
   IDX_COL7, 
   IDX_COL8, 
   XML_DOC_ID_NBR, 
   CREATE_MINT_CD, 
   LAST_UPDATE_SYSTEM_NM, 
   LAST_UPDATE_TMSTP) 
   values ('100000001', 
   'FX', 
   1198775361795, 
   4102380000000, 
   'FX', 
   '13020', 
   NULL, 
   '001', 
   1111111111111111113, 
   '1', 
   'APP4004', 
   CURRENT_TIMESTAMP); 
  
  
---------------------------------------------------------- 
-- expressApplyDiscountDetail 
insert into xml_doc_3 
   (XML_DOC_ID_NBR, 
   STRUCTURE_ID_NBR, 
   CREATE_MINT_CD, 
   MSG_PAYLOAD_QTY, 
   MSG_PAYLOAD1_IMG, 
   MSG_PAYLOAD2_IMG, 
   MSG_PAYLOAD_SIZE_NBR, 
   MSG_PURGE_DT, 
   DELETED_FLG, 
   LAST_UPDATE_SYSTEM_NM, 
   LAST_UPDATE_TMSTP, 
   MSG_MAJOR_VERSION_NBR, 
   MSG_MINOR_VERSION_NBR, 
   OPT_LOCK_TOKEN_NBR, 
   PRESET_DICTIONARY_ID_NBR) 
   values (1111111111111111114, 
   79, 
   '1', 
   1, 
   CAST(X'12345678452984560289456029847609487234785012934857109348156034650234560897628900985760289207856027895602785608560786085602857602985760206106110476191087345601456105610478568347562686289765927868972691785634975604562056104762978679451308956205620437861508561034756028475180756917856190348756012876510871789546913485620720476107856479238579385923847934' AS BLOB), 
   NULL, 
   350, 
   NULL, 
   'N', 
   'APP4004', 
   CURRENT_TIMESTAMP, 
   0, 
   5, 
   1, 
   0); 
  
-- /expressApplyDiscountDetail/accountNumber 
-- /expressApplyDiscountDetail/applyDiscount/effectiveDateTime 
-- /expressApplyDiscountDetail/applyDiscount/expirationDateTime 
insert into xml_idx_79_1 
   (IDX_COL1, 
   IDX_COL2, 
   IDX_COL3, 
   XML_DOC_ID_NBR, 
   CREATE_MINT_CD, 
   LAST_UPDATE_SYSTEM_NM, 
   LAST_UPDATE_TMSTP) 
   values ('100000001', 
   1198775361795, 
   4102380000000, 
   1111111111111111114, 
   '1', 
   'APP4004', 
   CURRENT_TIMESTAMP); 
  
  

