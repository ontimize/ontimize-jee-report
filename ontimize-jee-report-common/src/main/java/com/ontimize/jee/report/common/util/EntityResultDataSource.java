package com.ontimize.jee.report.common.util;

import com.ontimize.jee.common.dto.EntityResult;

public class EntityResultDataSource extends AbstractEntityResultDataSource<EntityResult> {

	public EntityResultDataSource(EntityResult result) {
        super(result);
    }

    @Override
    public int calculateIndex() {
        return index;
    }

}
