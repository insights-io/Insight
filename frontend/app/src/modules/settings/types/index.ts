export type PathPart = {
  segment: string;
  text: string;
};

export type Path = PathPart[];

export type SettingsLayoutPropsBase = {
  children: React.ReactNode;
  path: Path;
  header: string;
  title?: string;
};

export type SearchOption = { label: string; link: string; description: string };

export type SidebarSectionItem = {
  text: React.ReactNode;
  link: string;
};

export type SidebarSection = {
  header: string;
  items: SidebarSectionItem[];
};
