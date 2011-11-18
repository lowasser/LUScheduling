#!/usr/bin/env python
import os
import csv
import urllib
import optparse
import mechanize
import collections
import BeautifulSoup

from data_loader import login, list_programs

def main():
    parser = optparse.OptionParser()
    parser.add_option("-l", "--list-programs",
                      action="store_true", dest="list_programs")
    parser.add_option("-o", "--host", dest="host")
    parser.add_option("-u", "--username", dest="username")
    parser.add_option("-p", "--password", dest="password")
    parser.add_option("-r", "--program", dest="program")
    parser.add_option("-s", "--source-csv", dest="source_csv")

    (options, args) = parser.parse_args()

    if options.list_programs:
        list_programs(options.host, options.username, options.password)
    else:
        upload_assignments(options.host, options.username, options.password, options.program, options.source_csv)

def upload_assignments(host, username, password, program_string, source_csv):
    browser = login(host, username, password)

    url = 'https://%s/manage/%s/%s' % (
        host,
        program_string,
        'ajax_schedule_class',
        )

    if raw_input("Are you sure you want to load data to %s? (type yes) " % host).lower() != 'yes':
        print "You told me to stop."
        return

    section_assignments = collections.defaultdict(list)
    with open(source_csv, 'r') as source_file:
        reader = csv.reader(source_file)
        for row in reader:
            section_id, room_id, period_id = row
            section_assignments[section_id].append((period_id, room_id))

    if raw_input("About to schedule %s sections. Are you really sure? (type yes) " % len(section_assignments)).lower() != 'yes':
        print "You told me to stop."
        return

    for section_id, data in section_assignments.items():
        data.sort()
        post_data = urllib.urlencode({
                'cls': section_id,
                'action': 'assignreg',
                'block_room_assignments': '\n'.join('%s,%s' % block_room
                                                    for block_room in data),
            })

        # TEST THIS!
        browser.open(url, post_data)
        #print post_data


if __name__ == '__main__':
    main()
