export interface ValidationRule {
  id: string;
  validationName: string;
  active: boolean;
  objectName: string;
  errorMessage: string;
  description: string;
  entityDefinitionId: string;
  fullName: string;
}
