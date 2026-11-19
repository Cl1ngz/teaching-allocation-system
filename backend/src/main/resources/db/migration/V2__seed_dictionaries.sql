-- SF-14: initial values of the academic_degrees and positions dictionaries.
-- Both dictionaries are maintained by an administrator later on; changes to
-- these values go into new migrations or through the application, never by
-- editing this file.

-- rank_order uses steps of 10, so a degree can be inserted between two
-- existing ones without renumbering.
INSERT INTO academic_degrees (code, name, rank_order) VALUES
    ('MGR',    'magister',            10),
    ('DR',     'doktor',              20),
    ('DR_HAB', 'doktor habilitowany', 30),
    ('PROF',   'profesor',            40);

-- PROFESOR, PROFESOR_UCZELNI, ADIUNKT, ASYSTENT: positions defined in art. 116
-- of the Act on Higher Education and Science (Prawo o szkolnictwie wyższym i
-- nauce). The remaining ones are commonly created by university statutes
-- (art. 116 ust. 4) and are a proposal to confirm with the team.
INSERT INTO positions (code, name) VALUES
    ('PROFESOR',           'profesor'),
    ('PROFESOR_UCZELNI',   'profesor uczelni'),
    ('ADIUNKT',            'adiunkt'),
    ('ASYSTENT',           'asystent'),
    ('STARSZY_WYKLADOWCA', 'starszy wykładowca'),
    ('WYKLADOWCA',         'wykładowca'),
    ('LEKTOR',             'lektor'),
    ('INSTRUKTOR',         'instruktor');
