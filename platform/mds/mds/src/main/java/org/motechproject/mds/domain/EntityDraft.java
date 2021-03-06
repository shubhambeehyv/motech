package org.motechproject.mds.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.dto.AdvancedSettingsDto;
import org.motechproject.mds.dto.EntityDto;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class represents user drafts of an Entity. A draft is user's work in progress from the
 * UI. This shares the table with its superclass, {@link Entity}.
 */
@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.SUPERCLASS_TABLE)
public class EntityDraft extends Entity {

    @Persistent
    @Column(allowsNull = "false")
    private String draftOwnerUsername;

    @Persistent
    private DateTime lastModificationDate;

    @Persistent
    private Entity parentEntity;

    @Persistent
    private Long parentVersion;

    @Persistent
    private boolean changesMade;

    @Persistent
    private Map<String, String> fieldNameChanges;

    @Override
    public void updateAdvancedSetting(AdvancedSettingsDto advancedSettings) {
        updateIndexes(advancedSettings.getIndexes());
        updateBrowsingSettings(advancedSettings, true);
        updateRestOptions(advancedSettings);
        updateTracking(advancedSettings);
    }


    public EntityDraft() {
        fieldNameChanges = new HashMap<>();
    }

    public String getDraftOwnerUsername() {
        return draftOwnerUsername;
    }

    public void setDraftOwnerUsername(String draftOwnerUsername) {
        this.draftOwnerUsername = draftOwnerUsername;
    }

    public DateTime getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(DateTime lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public Entity getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(Entity parentEntity) {
        this.parentEntity = parentEntity;
    }

    public Long getParentVersion() {
        return parentVersion;
    }

    public void setParentVersion(Long parentVersion) {
        this.parentVersion = parentVersion;
    }

    public boolean isChangesMade() {
        return changesMade;
    }

    public void setChangesMade(boolean changesMade) {
        this.changesMade = changesMade;
    }

    public Map<String, String> getFieldNameChanges() {
        return fieldNameChanges;
    }

    public void setFieldNameChanges(Map<String, String> fieldNameChanges) {
        this.fieldNameChanges = fieldNameChanges;
    }

    @Override
    public EntityDto toDto() {
        EntityDto dto = super.toDto();
        dto.setModified(isChangesMade());
        dto.setOutdated(isOutdated());
        dto.setId(getParentEntity().getId());
        return dto;
    }

    @Override
    @NotPersistent
    public boolean isDraft() {
        return true;
    }

    @NotPersistent
    public boolean isOutdated() {
        return !Objects.equals(getParentVersion(), getParentEntity().getEntityVersion());
    }

    @Override
    public boolean isActualEntity() {
        return false;
    }
}
