import {
  MgmtFormGroup,
  DataRecord,
  MgmtFormControl,
  AbstractRegisteredService
} from 'mgmt-lib';
import {FormGroup} from '@angular/forms';

export class ExpirationForm extends FormGroup implements MgmtFormGroup<AbstractRegisteredService> {

  constructor(public data: DataRecord) {
    super({
      expirationDate: new MgmtFormControl(null),
      deleteWhenExpired: new MgmtFormControl(null),
      notifyWhenDeleted: new MgmtFormControl(null)
    });
    this.setValue(this.formMap());
  }

  formMap(): any {
    return {
      expirationDate: this.data.service.expirationPolicy.expirationDate,
      deleteWhenExpired: this.data.service.expirationPolicy.deleteWhenExpired,
      notifyWhenDeleted: this.data.service.expirationPolicy.notifyWhenDeleted
    }
  }

  mapForm(service: AbstractRegisteredService) {
    const frm = this.value;
    service.expirationPolicy.expirationDate = frm.expirationDate;
    service.expirationPolicy.deleteWhenExpired = frm.deleteWhenExpired;
    service.expirationPolicy.notifyWhenDeleted = frm.notifyWhenDeleted;
  }
}
