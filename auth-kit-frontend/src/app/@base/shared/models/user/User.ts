import { Role } from '../role/Role';

export interface User {
  id?: Number;
  firstName?: String;
  lastName?: String;
  username?: String;
  roles?: Role[];
  password?: String;
  email?: String;
  imageName?: String;
  imageAbsolutePath?: string;
}
