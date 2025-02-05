ALTER TABLE ft42dbadmin.users ADD user_state VARCHAR2(1);

CREATE INDEX idx_fallout_data_user_id ON fallout_data (ft42_user_id);

CREATE INDEX idx_fallout_data_mobile_no ON fallout_data (actual_mobile_no,
new_mobile_no,old_mobile_no);

commit