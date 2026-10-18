export interface AccessToken {
  tokenType: string;
  accessToken: string;
  expiresIn: number;
}

export interface SessionUser {
  id: number;
  name: string;
  email: string;
  role: 'ADMIN' | 'CUSTOMER';
  type: 'MEMBER' | 'NON_MEMBER';
}
