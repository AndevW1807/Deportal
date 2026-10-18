export type Membership = 'MEMBER' | 'NON_MEMBER';

export interface Customer {
  id: number;
  name: string;
  email: string;
  membership: Membership;
}
